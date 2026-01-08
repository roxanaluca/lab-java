package com.example.lab4.service;

import com.example.lab4.dta.StudentPreferenceCourseRegisterDta;
import com.example.lab4.dta.StudentPreferenceRegisterDta;
import com.example.lab4.dto.CourseDefaultDto;
import com.example.lab4.dto.PackDefaultDto;
import com.example.lab4.dto.PackDetailDto;
import com.example.lab4.dto.preference.StudentPreferenceDefaultDto;
import com.example.lab4.dto.preference.StudentPreferenceDetailDto;
import com.example.lab4.entity.Course;
import com.example.lab4.entity.Pack;
import com.example.lab4.entity.StudentPreference;
import com.example.lab4.entity.StudentPreferenceItem;
import com.example.lab4.entity.Student;
import com.example.lab4.errors.CourseDuplicate;
import com.example.lab4.errors.CourseNotFound;
import com.example.lab4.errors.PackNotFound;
import com.example.lab4.errors.PreferenceNotExist;
import com.example.lab4.errors.StudentNotFound;
import com.example.lab4.eventsourcing.PreferenceEventService;
import com.example.lab4.eventsourcing.projection.PreferenceProjection;
import com.example.lab4.eventsourcing.snapshot.SnapshotStore;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentPreferenceItemRepository;
import com.example.lab4.repository.PreferenceRepository;
import com.example.lab4.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentPreferenceService {
    @Autowired
    private PreferenceRepository preferenceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PackRepository packRepository;

    @Autowired
    private StudentPreferenceItemRepository studentPreferenceItemRepository;

    @Autowired
    PreferenceEventService preferenceEventService;

    private Long saveStudentPreferenceToDb(Long studentId, LinkedHashMap<Long, Long> map) throws PackNotFound, CourseNotFound, CourseDuplicate {
        Student student = studentRepository.getById(studentId);


        StudentPreference studentPreference = new StudentPreference();
        studentPreference.setStudent(student);
        preferenceRepository.save(studentPreference);
        studentPreference.setLastUpdated();

        int order = 0;
        HashSet<Course> courses = new HashSet<>();
        for (Map.Entry<Long, Long> registerDta : map.entrySet()) {
            StudentPreferenceItem studentPreferenceItem = new StudentPreferenceItem();
            studentPreferenceItem.setPreference(studentPreference);
            Optional<Course> course = courseRepository.findById(registerDta.getKey());

            if (course.isEmpty()) {
                preferenceRepository.delete(studentPreference);
                throw new CourseNotFound(registerDta.getKey().toString());
            }
            if (courses.contains(course.get())) {
                preferenceRepository.delete(studentPreference);
                throw new CourseDuplicate(course.get().getCode());
            }
            courses.add(course.get());

            if (registerDta.getValue() != null) {
                Optional<Pack> pack = packRepository.findById(registerDta.getValue());
                if (pack.isPresent()) {
                    Pack currentPack = pack.get();
//                    if (!currentPack.getCourses().contains(course.get())) {
//                        preferenceRepository.delete(studentPreference);
//                        throw new PackNotFound(registerDta.getValue());
//                    }
                    studentPreferenceItem.setPack(currentPack);
                } else {
                    throw new PackNotFound(registerDta.getValue());
                }
            }
            studentPreferenceItem.setCourse(course.get());
            studentPreferenceItem.setPreference(studentPreference);
            studentPreferenceItem.setRank(order);
            order++;
            studentPreferenceItemRepository.save(studentPreferenceItem);
        }
        return studentPreference.getId();
    }

    public StudentPreferenceDefaultDto register(StudentPreferenceRegisterDta studentPreferenceRegisterDta) throws StudentNotFound, CourseNotFound, CourseDuplicate, PackNotFound {

        StudentPreferenceDefaultDto studentDefaultDto = new StudentPreferenceDefaultDto();
        Student student = studentRepository.findByCode(studentPreferenceRegisterDta.studentCode());

        if (student == null)
            throw new StudentNotFound(studentPreferenceRegisterDta.studentCode());

        LinkedHashMap<Long, Long> courseAndPacksId =
                Arrays.stream(studentPreferenceRegisterDta.courseRegisterDta())
                        .collect(Collectors.toMap(
                                e -> {
                                    Course course = courseRepository.findByCode(e.courseCode());
                                    return course != null ? course.getId() : null;
                                },
                                e -> e.packId(),
                                (a, b) -> b,           // merge: keep last if duplicate keys happen
                                LinkedHashMap::new
                        ));
        String id = preferenceEventService.createPreference(student.getId(), courseAndPacksId);

        studentDefaultDto.setStudentCode(studentPreferenceRegisterDta.studentCode());
        studentDefaultDto.setPreferenceId(id);

        return studentDefaultDto;
    }

    public PackDefaultDto[] viewUnsubmittedPreference(String uuid) throws PreferenceNotExist {
        Optional<PreferenceProjection.PreferenceReadModel> aggregation = preferenceEventService.getPreference(uuid);

        if (aggregation.isEmpty()) {
            throw new PreferenceNotExist(uuid);
        }
        return getListAfterAggregation(aggregation.get().getCourseAndPacksId());
    }

    public void removeCourseUnsubmitted(String studentCode,String courseCode) throws CourseNotFound, StudentNotFound {
        Student student = studentRepository.findByCode(studentCode);

        if (student == null)
            throw new StudentNotFound(studentCode);
        Course course = courseRepository.findByCode(courseCode);
        if (course == null) {
            throw new CourseNotFound(courseCode);
        }
        Optional<PreferenceProjection.PreferenceReadModel> aggregation =  preferenceEventService.getPreferenceByStudent(student.getId());
        if (aggregation.isEmpty()) {
            throw new PreferenceNotExist("student preference not exist");
        }

        preferenceEventService.removeCourse(aggregation.get().getAggregateId(), course.getId());
    }

    public void addCourseUnsubmitted(String studentCode,String courseCode, Integer position, Long packId) throws CourseNotFound, StudentNotFound {
        Student student = studentRepository.findByCode(studentCode);

        if (student == null)
            throw new StudentNotFound(studentCode);
        Course course = courseRepository.findByCode(courseCode);
        if (course == null) {
            throw new CourseNotFound(courseCode);
        }
        Optional<PreferenceProjection.PreferenceReadModel> aggregation =  preferenceEventService.getPreferenceByStudent(student.getId());
        if (aggregation.isEmpty()) {
            throw new PreferenceNotExist("student preference not exist");
        }

        preferenceEventService.addCourse(aggregation.get().getAggregateId(),course.getId(),position,packId);
    }

    public String submitPreference(String studentCode) throws PackNotFound, CourseNotFound, CourseDuplicate, StudentNotFound {
        Student student = studentRepository.findByCode(studentCode);

        if (student == null)
            throw new StudentNotFound(studentCode);
        Optional<PreferenceProjection.PreferenceReadModel> aggregation =  preferenceEventService.getPreferenceByStudent(student.getId());
        if (aggregation.isEmpty()) {
            throw new PreferenceNotExist("student preference not exist");
        }
        String uuid = saveStudentPreferenceToDb(student.getId(), aggregation.get().getCourseAndPacksId()).toString();

         preferenceEventService.submitPreferences(aggregation.get().getAggregateId());
        return uuid;
    }

    public PackDefaultDto[] viewSubmittedPreference(Long preferenceId) throws StudentNotFound, CourseNotFound {
        Optional<StudentPreference> preference = preferenceRepository.findById(preferenceId);
        if (preference.isEmpty())
            throw new PreferenceNotExist(preferenceId.toString());
        if (preference.get().getStudent() == null)
            throw new StudentNotFound(preferenceId.toString());

        List<Pack> existentPacks = packRepository.groupByPreferenceId(preferenceId);
        List<PackDetailDto> packDetailDtos = new ArrayList<>();
        for (Pack pack : existentPacks) {
            PackDetailDto packDetailDto = new PackDetailDto();
            packDetailDto.setPackId(pack.getId());
            packDetailDto.setName(pack.getName());
            ArrayList<CourseDefaultDto> courseDetailDtos = new ArrayList<>();
            for (StudentPreferenceItem studentPreferenceItem : studentPreferenceItemRepository.findAllByPreferenceIdAndPackId(preferenceId, pack.getId())) {
                CourseDefaultDto courseDetailDto = new CourseDefaultDto();
                courseDetailDto.setCourseId(studentPreferenceItem.getCourse().getId());
                courseDetailDto.setCode(studentPreferenceItem.getCourse().getCode());
                courseDetailDto.setName(studentPreferenceItem.getCourse().getName());
                courseDetailDtos.add(courseDetailDto);
            }
            packDetailDto.setCourses(courseDetailDtos.toArray(new CourseDefaultDto[0]));
            packDetailDtos.add(packDetailDto);
        }
        PackDetailDto packDetailDto = new PackDetailDto();
        packDetailDto.setPackId(0L);
        packDetailDto.setName("Compulsory pack");
        ArrayList<CourseDefaultDto> courseDetailDtos = new ArrayList<>();
        for (Course course : courseRepository.findByPreferenceIdAndNotPack(preferenceId)) {
            CourseDefaultDto courseDetailDto = new CourseDefaultDto();
            courseDetailDto.setCourseId(course.getId());
            courseDetailDto.setCode(course.getCode());
            courseDetailDto.setName(course.getName());
            courseDetailDtos.add(courseDetailDto);
        }
        packDetailDto.setCourses(courseDetailDtos.toArray(new CourseDefaultDto[0]));
        packDetailDtos.add(packDetailDto);

        return packDetailDtos.toArray(new PackDetailDto[0]);
    }


    public PackDefaultDto[] viewSubmittedByStudentCode(String code) throws StudentNotFound, CourseNotFound {
        Student student = studentRepository.findByCode(code);

        if (student == null)
            throw new StudentNotFound(code);

        StudentPreference studentPreference = preferenceRepository.findLatestPreferenceByStudentId(student.getId());
        if (studentPreference == null)
            throw new PreferenceNotExist(
                    "0L");

        return this.viewSubmittedPreference(studentPreference.getId());
    }

    public StudentPreferenceDefaultDto[] viewAllHistory(String code) throws CourseNotFound, StudentNotFound {
        Student student = studentRepository.findByCode(code);
        if (student == null)
            throw new StudentNotFound(code);

        List<StudentPreference> studentPreferences = preferenceRepository.findAllByStudentId(student.getId());
        List<StudentPreferenceDefaultDto> studentPreferenceDefaultDtos = new ArrayList<>();
        for (StudentPreference studentPreference : studentPreferences) {
            StudentPreferenceDefaultDto studentPreferenceDefaultDto = new StudentPreferenceDefaultDto();
            studentPreferenceDefaultDto.setPreferenceId(studentPreference.getId().toString());
            studentPreferenceDefaultDto.setStudentCode(studentPreference.getStudent().getCode());
            studentPreferenceDefaultDtos.add(studentPreferenceDefaultDto);
        }
        return studentPreferenceDefaultDtos.toArray(new StudentPreferenceDefaultDto[0]);
    }

    public StudentPreferenceDefaultDto addSubmittedPreferenceItem(Long preferenceId, StudentPreferenceCourseRegisterDta preferenceRegisterDta) throws CourseDuplicate, CourseNotFound, PackNotFound {
        Optional<StudentPreference> preference = preferenceRepository.findById(preferenceId);
        StudentPreferenceItem preferenceItemOptional = studentPreferenceItemRepository.findByPreferenceIdAndCourseCode(preferenceId,preferenceRegisterDta.courseCode());
        if (preference.isEmpty() || preferenceItemOptional == null)
            throw new PreferenceNotExist(preferenceId.toString());
        Course course = courseRepository.findByCode(preferenceRegisterDta.courseCode());
        if (course == null)
            throw new CourseNotFound(preferenceRegisterDta.courseCode());
        if (studentPreferenceItemRepository.countByPreferenceIdAndCourseCode(preference.get().getId(), preferenceRegisterDta.courseCode()))
            throw new CourseDuplicate(preference.get().getStudent().getCode());
        Pack pack = null;
        if (preferenceRegisterDta.packId() != null) {
            if (packRepository.findById(preferenceRegisterDta.packId()).isEmpty()) {
                throw new PackNotFound(preferenceRegisterDta.packId());
            }
            pack = packRepository.findById(preferenceRegisterDta.packId()).get();
        }

        studentPreferenceItemRepository.findByPreferenceIdAndCourseCode(preference.get().getId(), preferenceRegisterDta.courseCode()).getStudentPreference().getPreferenceList().forEach(
                item -> {
                    if (item.getRank() >= preferenceRegisterDta.position()) {
                        item.setRank(item.getRank() + 1);
                        studentPreferenceItemRepository.save(item);
                    }
                }
        );


        preferenceItemOptional.setCourse(course);
        preferenceItemOptional.setPack(pack);
        studentPreferenceItemRepository.save(preferenceItemOptional);

        StudentPreferenceDetailDto preferenceDefaultDto = new StudentPreferenceDetailDto();
        preferenceDefaultDto.setPreferenceId(preference.get().getId().toString());
        preferenceDefaultDto.setStudentCode(preference.get().getStudent().getCode());
        preferenceDefaultDto.setCourseCode(course.getCode());
        preferenceDefaultDto.setPackCode(pack == null ? null : pack.getId());

        return preferenceDefaultDto;
    }


    public void deleteSubmittedPreferenceItem(Long preferenceId, String courseCode) throws StudentNotFound, CourseNotFound, PackNotFound {
        Optional<StudentPreference> preference = preferenceRepository.findById(preferenceId);
        StudentPreferenceItem preferenceItemOptional = studentPreferenceItemRepository.findByPreferenceIdAndCourseCode(preferenceId,courseCode);
        if (preference.isEmpty() || preferenceItemOptional == null)
            throw new PreferenceNotExist(preferenceId.toString());
        Course course = courseRepository.findByCode(courseCode);
        if (course == null)
            throw new CourseNotFound(courseCode);

        studentPreferenceItemRepository.delete(preferenceItemOptional);
    }

    public void deleteSubmittedPreference(Long id) {
        Optional<StudentPreference> preference = preferenceRepository.findById(id);
        if (preference.isEmpty()) {
            throw new PreferenceNotExist(id.toString());
        }
        preferenceRepository.deleteById(preference.get().getId());
    }

    public String getSubmittedStudentCode(Long id) {
        Optional<StudentPreference> preference = preferenceRepository.findById(id);
        if (preference.isEmpty())
            throw new PreferenceNotExist(id.toString());
        return preference.get().getStudent().getCode();
    }

    public String getUnSubmittedStudentCode(String id) throws StudentNotFound {
        Optional<PreferenceProjection.PreferenceReadModel> preference = preferenceEventService.getPreference(id);
        if (preference.isEmpty())
            throw new PreferenceNotExist(id.toString());
        return studentRepository.findById(preference.get().getStudentId()).map(Student::getCode)
                .orElseThrow(() ->new StudentNotFound(preference.get().getStudentId().toString()));
    }

    private PackDetailDto[] getListAfterAggregation(Map<Long,Long> courseAndPacks) {
        HashSet<Long> packs = new HashSet<>(courseAndPacks.values());
        List<PackDetailDto> packDetailDtos = new ArrayList<>();
        for (Long packId : packs) {
            PackDetailDto packDetailDto = new PackDetailDto();
            packDetailDto.setPackId(packId);
            Pack pack = packRepository.findById(packId).orElse(null);
            packDetailDto.setName(pack != null ? pack.getName() : "Compulsory pack");
            ArrayList<CourseDefaultDto> courseDetailDtos = new ArrayList<>();
            for (Map.Entry<Long, Long> course :  courseAndPacks.entrySet().stream().filter(e -> e.getValue().equals(packId)).toList()) {
                CourseDefaultDto courseDefaultDto = new CourseDefaultDto();
                Course currentCourse = courseRepository.findById(course.getKey()).orElse(null);
                courseDefaultDto.setCourseId(course.getKey());
                courseDefaultDto.setName(currentCourse != null ? currentCourse.getName() : "Unknown Course");
                courseDefaultDto.setCode(currentCourse != null ? currentCourse.getCode() : "Unknown Course");
                courseDetailDtos.add(courseDefaultDto);
            }
            packDetailDto.setCourses(courseDetailDtos.toArray(new CourseDefaultDto[0]));

            packDetailDtos.add(packDetailDto);
        }
        return packDetailDtos.toArray(new PackDetailDto[0]);
    }

    public List<PackDetailDto[]> viewHistory(String code) throws StudentNotFound, CourseNotFound {
        Student student = studentRepository.findByCode(code);
        if (student == null)
            throw new StudentNotFound(code);
        Optional<PreferenceProjection.PreferenceReadModel> aggregation =  preferenceEventService.getPreferenceByStudent(student.getId());

        if (aggregation.isEmpty())
            throw new PreferenceNotExist(code);

        Optional<SnapshotStore.Snapshot> preference = preferenceEventService.getSnapshot(aggregation.get().getAggregateId());

        return preference.stream().map(e -> getListAfterAggregation(e.courseIds())).collect(Collectors.toList());
    }
}
