package com.example.lab4.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Embeddable
class StudentCourseId {
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "student_id")
    private Long studentId;

    public StudentCourseId(){}

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public Long getCourseId() {
        return courseId;
    }
    public Long getStudentId() {
        return studentId;
    }
}

@Entity
@Table(name = "student_course")
public class StudentCourse {
    @EmbeddedId
    private StudentCourseId id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    public StudentCourse() {
        id = new StudentCourseId();
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        id.setCourseId(course.getId());
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
        id.setStudentId(student.getId());
    }

    public StudentCourseId getId() {
        return id;
    }
}
