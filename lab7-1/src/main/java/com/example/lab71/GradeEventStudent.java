package com.example.lab71;


public class GradeEventStudent extends GradeEvent {
    private Long studentId;
    private String studentName;
    private int studentYear;

    public GradeEventStudent(GradeEvent event) {
        super(event.getStudentCode(), event.getCourseCode(), event.getGrade());
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getStudentYear() {
        return studentYear;
    }

    public void setStudentYear(int studentYear) {
        this.studentYear = studentYear;
    }
}
