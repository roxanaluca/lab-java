package com.example.lab71;

import java.math.BigDecimal;

public class GradeEvent {

    private String studentCode;
    private String courseCode;
    private BigDecimal grade;

    public GradeEvent() {
    }

    public GradeEvent(String studentCode, String courseCode, BigDecimal grade) {
        this.studentCode = studentCode;
        this.courseCode = courseCode;
        this.grade = grade;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }
}