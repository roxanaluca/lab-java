package com.example.lab4.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "preferences")
public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @OneToMany(
            mappedBy = "studentPreference",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<StudentPreferenceItem> preferenceList;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<StudentPreferenceItem> getPreferenceList() {
        return preferenceList;
    }

    public void setPreferenceList(List<StudentPreferenceItem> preferenceList) {
        this.preferenceList = preferenceList;
    }

    @LastModifiedDate
    private Instant lastUpdated;
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated() {
        this.lastUpdated = Instant.now();
    }
}
