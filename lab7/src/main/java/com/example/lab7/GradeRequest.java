package com.example.lab7;

import java.math.BigDecimal;

public class GradeRequest {

    private String studentCode;
    private String courseCode;
    private BigDecimal grade;

    public GradeRequest() { }

    public String getStudentCode() {
        return studentCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }
}