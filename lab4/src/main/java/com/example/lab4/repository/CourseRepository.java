package com.example.lab4.repository;

import com.example.lab4.entity.Course;
import com.example.lab4.entity.Instructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE c.pack.year = :year AND c.pack.semester = :semester")
    List<Course> findByPackYearAndSemester(@Param("year") int year, @Param("semester") int semester);

    @Query("SELECT c FROM Course c WHERE c.instructor = :instructor")
    List<Course> findByInstructor(@Param("instructor") Instructor instructor);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.instructor.id = :instructorId WHERE c.id = :courseId")
    void updateInstructorForCourse(@Param("courseId") Long courseId, @Param("instructorId") Long instructorId);

    @Modifying
    @Transactional
    void deleteByCode(String code);

    Course findByCode(String code);


    @Query(" SELECT sc.course FROM StudentCourse sc WHERE sc.student.id = :studentId\n AND sc.course.pack IS NULL")
    List<Course> findByStudentIdAndNotPack(Long studentId);

    @Query("SELECT c FROM Course c JOIN c.students s  WHERE s.id = :studentId")
    Set<Course> findCoursesByStudent(Long studentId);

    @Query("SELECT c from Course c inner join StudentPreferenceItem pt on pt.course.id = c.id and pt.studentPreference.id = :preferenceId where pt.pack = null ")
    List<Course> findByPreferenceIdAndNotPack(Long preferenceId);

    boolean existsByCode(String code);

    @Query("select c from Course c left join fetch c.students where c.id = :id")
    Optional<Course> findByIdWithStudents(Long id);

    @Query("select c from Course c left join fetch c.students")
    List<Course> findAllWithStudents();
}
