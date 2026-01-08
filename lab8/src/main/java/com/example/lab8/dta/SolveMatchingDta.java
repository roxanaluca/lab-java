package com.example.lab8.dta;

import java.util.List;
import java.util.Map;

public record SolveMatchingDta(
        String packId,
        Algorithm algorithm,
        List<StudentDta> students,
        List<CourseDta> courses,
        Map<String, List<String>> courseRankings
) {}