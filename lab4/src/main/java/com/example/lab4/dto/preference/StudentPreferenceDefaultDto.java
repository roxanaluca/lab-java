package com.example.lab4.dto.preference;

import org.springframework.hateoas.RepresentationModel;

public class StudentPreferenceDefaultDto extends RepresentationModel<StudentPreferenceDefaultDto> {
    String studentCode;
    String preferenceId;

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }
}
