package com.example.lab72;

import org.springframework.data.jpa.repository.JpaRepository;



public interface CourseRepository extends JpaRepository<Course, Long> {

    Course findByCode(String code);
}