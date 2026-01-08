package com.example.lab8.service;

import com.example.lab8.dta.CourseDta;
import com.example.lab8.dta.SolveMatchingDta;
import com.example.lab8.dta.StudentDta;
import com.example.lab8.dto.AssignmentDto;
import com.example.lab8.dto.SolveMatchingDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GaleShapleyMatching implements Matching {


    @Override
    public  SolveMatchingDto solveMatching( SolveMatchingDta req) {

        // courseId -> (studentId -> rank index); lower = better
        Map<String, Map<String, Integer>> rankIndex = buildRankIndex(req.courseRankings());

        Map<String, Integer> capacity = req.courses().stream()
                .collect(Collectors.toMap(CourseDta::courseId, CourseDta::capacity));

        // courseId -> currently held studentIds
        Map<String, List<String>> held = new HashMap<>();
        for (CourseDta c : req.courses()) held.put(c.courseId(), new ArrayList<>());

        // studentId -> next preference index
        Map<String, Integer> nextPref = new HashMap<>();
        for (StudentDta s : req.students()) nextPref.put(s.studentId(), 0);

        // queue of free students who still can propose
        Deque<StudentDta> free = new ArrayDeque<>(req.students());

        while (!free.isEmpty()) {
            StudentDta s = free.removeFirst();
            int i = nextPref.get(s.studentId());

            if (s.preferences() == null || i >= s.preferences().size()) {
                continue; // no more proposals possible => remains unassigned
            }

            String courseId = s.preferences().get(i);
            nextPref.put(s.studentId(), i + 1);

            // if course doesn't exist, skip and keep proposing
            if (!capacity.containsKey(courseId)) {
                free.addLast(s);
                continue;
            }

            held.get(courseId).add(s.studentId());

            // if over capacity, reject worst until capacity satisfied
            int cap = capacity.get(courseId);
            if (held.get(courseId).size() > cap) {
                String worst = pickWorst(courseId, held.get(courseId), rankIndex);
                held.get(courseId).remove(worst);

                // rejected student becomes free again (if they still have prefs)
                StudentDta rejected = findStudent(req.students(), worst);
                if (rejected != null) {
                    int next = nextPref.get(rejected.studentId());
                    if (rejected.preferences() != null && next < rejected.preferences().size()) {
                        free.addLast(rejected);
                    }
                }
            }
        }

        // build assignments
        List<AssignmentDto> assignments = new ArrayList<>();
        Set<String> assignedStudents = new HashSet<>();

        for (var e : held.entrySet()) {
            String courseId = e.getKey();
            for (String studentId : e.getValue()) {
                assignments.add(new AssignmentDto(studentId, courseId));
                assignedStudents.add(studentId);
            }
        }

        List<String> unassigned = req.students().stream()
                .map(StudentDta::studentId)
                .filter(id -> !assignedStudents.contains(id))
                .toList();

        return new SolveMatchingDto(
                req.packId(),
                req.algorithm(),
                assignments,
                unassigned,
                "Gale-Shapley stable matching with capacities (students propose)."
        );
    }

    private static Map<String, Map<String, Integer>> buildRankIndex(Map<String, List<String>> rankings) {
        Map<String, Map<String, Integer>> out = new HashMap<>();
        if (rankings == null) return out;

        for (var e : rankings.entrySet()) {
            Map<String, Integer> idx = new HashMap<>();
            List<String> list = e.getValue() == null ? List.of() : e.getValue();
            for (int i = 0; i < list.size(); i++) idx.put(list.get(i), i);
            out.put(e.getKey(), idx);
        }
        return out;
    }

    private static String pickWorst(String courseId, List<String> candidates, Map<String, Map<String, Integer>> rankIndex) {
        Map<String, Integer> idx = rankIndex.getOrDefault(courseId, Map.of());

        // if instructor ranking missing a student, treat as very bad (large index)
        return candidates.stream()
                .max(Comparator.comparingInt(sid -> idx.getOrDefault(sid, Integer.MAX_VALUE)))
                .orElseThrow();
    }

    private static StudentDta findStudent(List<StudentDta> students, String studentId) {
        for (StudentDta s : students) if (s.studentId().equals(studentId)) return s;
        return null;
    }
}
