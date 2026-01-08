package com.example.lab4.dta;

public record UpdateCourseRequest(
        String name,
        String description,
        Integer capacity
) {}