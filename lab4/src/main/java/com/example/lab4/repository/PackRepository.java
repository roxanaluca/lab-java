package com.example.lab4.repository;

import com.example.lab4.entity.Pack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface PackRepository  extends JpaRepository<Pack, Long> {

    @Query("SELECT DISTINCT p FROM Pack p WHERE EXISTS (SELECT 1 FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.course.id IN (SELECT c.id FROM p.courses c))")
    List<Pack> findByStudentId(Long studentId);

    @Query("SELECT DISTINCT p from Pack p inner join StudentPreferenceItem pt on pt.pack = p and pt.studentPreference.id = :preferenceId")
    List<Pack> groupByPreferenceId(Long preferenceId);

    @Modifying
    @Transactional
    @Query("UPDATE Pack p SET p.lastUpdated = :newTime WHERE p.id = :packId")
    void updateLastUpdatedById(Long packId, Instant newTime);

    @Query("SELECT p from Pack p where EXISTS(SELECT c FROM p.courses c WHERE c.code = :courseCode)")
    Pack findByCourseCode(String courseCode);
}
