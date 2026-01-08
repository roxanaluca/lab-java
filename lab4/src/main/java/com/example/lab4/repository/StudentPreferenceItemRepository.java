package com.example.lab4.repository;

import com.example.lab4.entity.StudentPreferenceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

public interface StudentPreferenceItemRepository extends JpaRepository<StudentPreferenceItem, Long> {

    @Query("SELECT pt from StudentPreferenceItem pt where pt.studentPreference.id = :preferenceId and pt.pack.id = :packId")
    List<StudentPreferenceItem> findAllByPreferenceIdAndPackId(Long preferenceId, Long packId);

    @Query("SELECT count(pt) from StudentPreferenceItem pt where pt.studentPreference.id = :preferenceId and pt.course.code = :courseCode")
    boolean countByPreferenceIdAndCourseCode(Long preferenceId, String courseCode);

    @Query("SELECT pt from StudentPreferenceItem pt where pt.course.pack.id = :packId and pt.studentPreference.lastUpdated = (SELECT MAX(p2.lastUpdated) FROM StudentPreference p2 where p2.id = pt.studentPreference.id)")
    List<StudentPreferenceItem> findAllByPackId(Long packId);

    @Query("select  pt from InstructorPreferenceItem pt where pt.course.code = :courseCode")
    StudentPreferenceItem findByPreferenceIdAndCourseCode(Long preferenceId, String courseCode);
}
