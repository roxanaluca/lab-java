package com.example.lab4.errors;

public class StudentNotFound extends Exception {
    String studentCode;
    public StudentNotFound(String studentCode) {
        super("Student not found");
        this.studentCode = studentCode;
    }

    public String getStudentCode() {
        return studentCode;
    }
}
