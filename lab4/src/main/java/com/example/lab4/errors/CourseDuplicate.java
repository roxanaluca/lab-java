package com.example.lab4.errors;

public class CourseDuplicate extends Exception {
    String courseCode;
    public CourseDuplicate(String studentCode) {
        super("Course is already in use");
        this.courseCode = studentCode;
    }

    public String getCourseCode() {
        return courseCode;
    }
}
