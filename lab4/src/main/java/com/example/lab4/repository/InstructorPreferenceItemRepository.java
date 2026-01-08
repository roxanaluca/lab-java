package com.example.lab4.repository;

import com.example.lab4.entity.InstructorPreferenceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstructorPreferenceItemRepository extends JpaRepository<InstructorPreferenceItem, Long> {
    @Query("SELECT pt from InstructorPreferenceItem pt where pt.instructorPreference.id = :preferenceId")
    List<InstructorPreferenceItem> findAllByPreferenceId(Long preferenceId);

    @Query("SELECT pt from InstructorPreferenceItem pt where pt.course.code = :code")
    InstructorPreferenceItem findAllByCourseCode(String code);

    @Query("SELECT pt from InstructorPreferenceItem pt where pt.course.pack.id = :packId")
    List<InstructorPreferenceItem> findAllByPackId(Long packId);
}
