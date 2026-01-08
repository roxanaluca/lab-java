package com.example.lab4.service;

import com.example.lab4.entity.ScheduleSaga;
import com.example.lab4.entity.StudentCourse;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.CourseStudentRepository;
import com.example.lab4.repository.ScheduleSagaRepository;
import com.example.lab4.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleSagaService {

    private final ScheduleSagaRepository sagaRepo;
    private final CourseStudentRepository courseStudentRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final InstructorService instructorService;
    private final ObjectMapper objectMapper;

    public ScheduleSagaService(
            ScheduleSagaRepository sagaRepo,
            CourseStudentRepository courseStudentRepo,
            StudentRepository studentRepo,
            CourseRepository courseRepo,
            InstructorService instructorService,
            ObjectMapper objectMapper
    ) {
        this.sagaRepo = sagaRepo;
        this.courseStudentRepo = courseStudentRepo;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.instructorService = instructorService;
        this.objectMapper = objectMapper;
    }

    public String startScheduleSaga(Long packId, String algorithm) {
        String sagaId = UUID.randomUUID().toString();

        ScheduleSaga saga = new ScheduleSaga();
        saga.setSagaId(sagaId);
        saga.setPackId(packId);
        saga.setAlgorithm(algorithm);
        saga.setStatus(ScheduleSaga.Status.STARTED);
        sagaRepo.save(saga);

        try {
            // STEP 1 (RETRIABLE): call StableMatch
            String raw = instructorService.matchPreference(packId, algorithm);
            saga.setStableMatchRawResult(raw);
            saga.setStatus(ScheduleSaga.Status.STABLEMATCH_OK);
            sagaRepo.save(saga);

            // STEP 2 (COMPENSATABLE): apply matching in DB (with snapshot)
            applyMatchingWithSnapshot(sagaId, packId, raw);
            saga.setStatus(ScheduleSaga.Status.DB_APPLIED);
            sagaRepo.save(saga);

            // STEP 3: done (you can also publish a Kafka event here)
            saga.setStatus(ScheduleSaga.Status.COMPLETED);
            sagaRepo.save(saga);

        } catch (Exception e) {
            saga.setError(e.getMessage());

            // If DB already applied and you fail afterwards, compensate
            if (saga.getStatus() == ScheduleSaga.Status.DB_APPLIED) {
                saga.setStatus(ScheduleSaga.Status.COMPENSATING);
                sagaRepo.save(saga);
                try {
                    compensateRestoreSnapshot(sagaId, packId);
                    saga.setStatus(ScheduleSaga.Status.COMPENSATED);
                } catch (Exception compEx) {
                    saga.setError(saga.getError() + " | COMPENSATION FAILED: " + compEx.getMessage());
                    saga.setStatus(ScheduleSaga.Status.FAILED);
                }
            } else {
                saga.setStatus(ScheduleSaga.Status.FAILED);
            }

            sagaRepo.save(saga);
            return sagaId;
        }
        return saga.getStatus().toString();
    }

    @Transactional
    protected void applyMatchingWithSnapshot(String sagaId, Long packId, String rawStableMatchResult) throws Exception {
        ScheduleSaga saga = sagaRepo.findById(sagaId).orElseThrow();

        // snapshot BEFORE state
        List<StudentCourse> before = courseStudentRepo.findAllByPackId(packId);
        List<Assignment> beforeSnapshot = before.stream()
                .map(sc -> new Assignment(sc.getStudent().getCode(), sc.getCourse().getCode()))
                .collect(Collectors.toList());
        saga.setBeforeAssignmentsJson(objectMapper.writeValueAsString(beforeSnapshot));
        sagaRepo.save(saga);

        // delete existing assignments for pack
        if (!before.isEmpty()) {
            courseStudentRepo.deleteAllByPackId(packId);
        }
        // parse StableMatch result -> list of (studentId, courseId)
        List<Assignment> newAssignments = parseAssignments(rawStableMatchResult).assignments;

        // apply new assignments (idempotent approach: we already deleted for pack)
        for (Assignment a : newAssignments) {
            StudentCourse sc = new StudentCourse();
            sc.setStudent(studentRepo.findByCode(a.studentId()));
            sc.setCourse(courseRepo.findByCode(a.courseId()));
            courseStudentRepo.save(sc);
        }
    }

    @Transactional
    protected void compensateRestoreSnapshot(String sagaId, Long packId) throws Exception {
        ScheduleSaga saga = sagaRepo.findById(sagaId).orElseThrow();
        String json = saga.getBeforeAssignmentsJson();
        if (json == null || json.isBlank()) {
            // nothing to restore
            courseStudentRepo.deleteAllByPackId(packId);
            return;
        }

        List<Assignment> before = objectMapper.readValue(json, new TypeReference<>() {});
        courseStudentRepo.deleteAllByPackId(packId);

        for (Assignment a : before) {
            StudentCourse sc = new StudentCourse();
            sc.setStudent(studentRepo.findByCode(a.studentId()));
            sc.setCourse(courseRepo.findByCode(a.courseId()));
            courseStudentRepo.save(sc);
        }
    }



    // Adjust this to whatever StableMatch returns
    private ResultAssignment parseAssignments(String raw) throws Exception {

        return objectMapper.readValue(raw, new TypeReference<ResultAssignment>() {});
    }

    public record ResultAssignment(List<Assignment> assignments, Instant createdAt, String matchingId) {}
    public record Assignment(String studentId, String courseId) {}
}