package com.example.lab4.errors;

public class CourseOutOfLimit extends Exception{
    String courseCode;
    public CourseOutOfLimit(String studentCode) {
        super("Course not found");
        this.courseCode = studentCode;
    }

    public String getCourseCode() {
        return courseCode;
    }
}
