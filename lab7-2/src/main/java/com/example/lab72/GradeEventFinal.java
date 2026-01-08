package com.example.lab72;

public class GradeEventFinal extends GradeEventStudent {

    public GradeEventFinal(GradeEventStudent event) {
        super();
        this.setStudentId(event.getStudentId());
        this.setStudentName(event.getStudentName());
        this.setStudentYear(event.getStudentYear());
    }
    public GradeEventFinal() {
        super();
    }


    private Long courseId;

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    private int semester;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    private String courseName;
}
