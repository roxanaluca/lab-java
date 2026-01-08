package com.example.lab4.repository;

import com.example.lab4.entity.StudentPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PreferenceRepository extends JpaRepository<StudentPreference, Long> {

    StudentPreference findByStudentId(Long studentId);

    @Query("SELECT p from StudentPreference p where p.student.id = :studentId and p.lastUpdated = (SELECT MAX(p2.lastUpdated) FROM StudentPreference p2 where p.student.id = :studentId )")
    StudentPreference findLatestPreferenceByStudentId(Long studentId);

    List<StudentPreference> findAllByStudentId(Long studentId);
}
