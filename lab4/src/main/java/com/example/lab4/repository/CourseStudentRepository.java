package com.example.lab4.repository;

import com.example.lab4.entity.Course;
import com.example.lab4.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseStudentRepository extends JpaRepository<StudentCourse, Long> {
    @Query("SELECT count(cs) FROM StudentCourse cs WHERE cs.course.id = :courseId")
    int countStudentForCourse(Long courseId);

    void deleteByStudentId(Long studentId);

    @Query("SELECT c FROM Course c WHERE c.pack.id = :packId AND EXISTS (SELECT cs FROM StudentCourse cs WHERE cs.course = c AND cs.student.id = :studentId)")
    List<Course> findByCourseByPackAndStudentId(Long packId, Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("SELECT cs FROM StudentCourse cs WHERE cs.course.pack.id = :packId")
    List<StudentCourse> findAllByPackId(@Param("packId") Long packId);

    @Modifying
    @Query("DELETE FROM StudentCourse cs WHERE cs.course.pack.id = :packId")
    @Transactional
    void deleteAllByPackId(@Param("packId") Long packId);
}
