package com.example.lab4.dta;

import jakarta.annotation.Nullable;

public record StudentPreferenceCourseRegisterDta(String courseCode, @Nullable Integer position, @Nullable Long packId){}
