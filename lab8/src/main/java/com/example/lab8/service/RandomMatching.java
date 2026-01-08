package com.example.lab8.service;

import com.example.lab8.dta.CourseDta;
import com.example.lab8.dta.SolveMatchingDta;
import com.example.lab8.dta.StudentDta;
import com.example.lab8.dto.AssignmentDto;
import com.example.lab8.dto.SolveMatchingDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RandomMatching implements Matching {

    @Override
    public SolveMatchingDto solveMatching(SolveMatchingDta req) {
        Map<String, Integer> remaining = req.courses().stream()
                .collect(Collectors.toMap(CourseDta::courseId, CourseDta::capacity));

        List<StudentDta> students = new ArrayList<>(req.students());
        Collections.shuffle(students);

        List<String> courseIds = req.courses().stream().map(CourseDta::courseId).toList();

        List<AssignmentDto> assignments = new ArrayList<>();
        List<String> unassigned = new ArrayList<>();

        Random rnd = new Random();

        for (StudentDta s : students) {
            List<String> shuffledCourses = new ArrayList<>(courseIds);
            Collections.shuffle(shuffledCourses, rnd);

            String chosen = null;
            for (String c : shuffledCourses) {
                if (remaining.getOrDefault(c, 0) > 0) {
                    chosen = c;
                    break;
                }
            }

            if (chosen == null) {
                unassigned.add(s.studentId());
            } else {
                remaining.put(chosen, remaining.get(chosen) - 1);
                assignments.add(new AssignmentDto(s.studentId(), chosen));
            }
        }

        return new SolveMatchingDto(
                req.packId(),
                req.algorithm(),
                assignments,
                unassigned,
                "Random matching (ignores preferences)."
        );
    }
}
