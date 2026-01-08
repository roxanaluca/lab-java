package com.example.lab4.repository;

import com.example.lab4.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByYear(int year);
    Student findByCode(String code);

    void deleteByCode(String code);

    Student findByEmail(String email);

    boolean existsByCode(String code);
}
