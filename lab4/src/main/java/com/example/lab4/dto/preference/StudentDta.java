package com.example.lab4.dto.preference;

import java.util.List;

public record StudentDta(
        String studentId,
        List<String> preferences
) {}