package com.example.lab8.dta;

import java.util.List;

public record StudentDta(
        String studentId,
        List<String> preferences
) {}