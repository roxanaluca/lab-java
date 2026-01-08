package com.example.lab4.dta;

public record CreateCourseRequest(
        String name,
        String code,
        String abbr,
        String type,
        String description,
        Integer capacity,
        Long instructorId,
        Long packId
) {}