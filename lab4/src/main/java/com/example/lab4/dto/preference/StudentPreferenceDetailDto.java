package com.example.lab4.dto.preference;

public class StudentPreferenceDetailDto extends StudentPreferenceDefaultDto {
    String courseCode;
    Long packCode;

    public Long getPreferenceItemId() {
        return preferenceItemId;
    }

    public void setPreferenceItemId(Long preferenceItemId) {
        this.preferenceItemId = preferenceItemId;
    }

    Long preferenceItemId;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Long getPackCode() {
        return packCode;
    }

    public void setPackCode(Long packCode) {
        this.packCode = packCode;
    }

}
