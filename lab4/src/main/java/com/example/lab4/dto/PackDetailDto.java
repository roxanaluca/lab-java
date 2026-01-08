package com.example.lab4.dto;

public class PackDetailDto extends PackDefaultDto {
    CourseDefaultDto[] courses;
    public CourseDefaultDto[] getCourses() {
        return courses;
    }
    public void setCourses(CourseDefaultDto[] courses) {
        this.courses = courses;
    }
}
