package com.example.lab4.dto;

import org.springframework.hateoas.RepresentationModel;

public class StudentDefaultDto  extends RepresentationModel<StudentDefaultDto> {
     Long studentId;


    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}