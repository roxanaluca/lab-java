package com.example.lab4.repository;

import com.example.lab4.entity.InstructorPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstructorPreferenceRepository  extends JpaRepository<InstructorPreference, Long> {

    @Query("SELECT pref from InstructorPreference pref inner join pref.instructorPreferenceItems pt inner join Course c on pt.course = c where pt.course.pack.id = :id")
    public List<InstructorPreference> findAllByPackId(Long id );

    @Query("select pref from InstructorPreference pref where pref.courseOptionalName = :courseCode")
    public InstructorPreference findByCourseCode(String courseCode);
}
