package com.example.lab4.errors;

public class CourseNotFound extends Exception {
    String courseCode;
    public CourseNotFound(String studentCode) {
        super("Course not found");
        this.courseCode = studentCode;
    }

    public String getCourseCode() {
        return courseCode;
    }
}
