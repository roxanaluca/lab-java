package com.example.lab4.repository;

import com.example.lab4.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g from Grade g where g.course.code = :courseCode and g.student.code = :studentCode")
    List<Grade>findByStudentCodeAndCourseCode(String studentCode, String courseCode);

    @Query("SELECT g from Grade g where g.student.code = :studentCode")
    List<Grade> findByStudentCode(String studentCode);
}
