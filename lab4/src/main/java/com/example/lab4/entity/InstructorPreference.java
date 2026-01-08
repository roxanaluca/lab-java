package com.example.lab4.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name="course_preferences")
public class InstructorPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String courseOptionalName;

    @OneToMany(
            mappedBy = "instructorPreference",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<InstructorPreferenceItem> instructorPreferenceItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseOptionalName() {
        return courseOptionalName;
    }

    public void setCourseOptionalName(String courseOptionalName) {
        this.courseOptionalName = courseOptionalName;
    }

    public List<InstructorPreferenceItem> getInstructorPreferenceItems() {
        return instructorPreferenceItems;
    }
}
