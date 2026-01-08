package com.example.lab4.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_preference_items")
public class InstructorPreferenceItem {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(Long weightPercent) {
        this.weightPercent = weightPercent;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public InstructorPreference getInstructorPreference() {
        return instructorPreference;
    }

    public void setInstructorPreference(InstructorPreference instructorPreference) {
        this.instructorPreference = instructorPreference;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_id")
    private InstructorPreference instructorPreference;


    @Column(name="weight_percent", nullable=false)
    private Long weightPercent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "optional_course_code",
            referencedColumnName = "code",
            nullable = false,
            unique = true
    )
    private Course course;
}
