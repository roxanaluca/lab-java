package com.example.lab4.dto;

public class StudentCourseDetailDto extends StudentDefaultDto {
    PackDetailDto[] packs;
    public PackDetailDto[] getPacks() {
        return packs;
    }
    public void setPacks(PackDetailDto[] packs) {
        this.packs = packs;
    }
}
