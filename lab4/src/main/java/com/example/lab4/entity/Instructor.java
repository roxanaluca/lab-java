package com.example.lab4.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name="instructors")
public class Instructor extends User{
    @SequenceGenerator(name = "sequence",
            sequenceName = "persons_id_seq")
    @GeneratedValue(generator = "sequence")
    @Column(name = "id")
    Long id;
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Course> courses;

    public List<Course> getCourses() {
        return courses;
    }
    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
