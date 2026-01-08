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
class PreferenceItemId {

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "preference_id")
    private Long preferenceId;

    public PreferenceItemId(){}

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

}
@Entity
@Table(name = "preference_items")
public class StudentPreferenceItem {

    @EmbeddedId
    private PreferenceItemId id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @MapsId("preferenceId")
    @JoinColumn(name = "preference_id")
    private StudentPreference studentPreference;

    @ManyToOne
    @JoinColumn(name = "pack_id", nullable = true)
    private Pack pack;

    @Column
    private double rank;

    public StudentPreferenceItem(){
        id = new PreferenceItemId();
    }

    public void setCourse(Course course) {
        this.course = course;
        this.id.setCourseId(course.getId());
    }

    public StudentPreference getStudentPreference() {
        return studentPreference;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public double getRank(){
        return this.rank;
    }

    public void setPreference(StudentPreference studentPreference) {
        this.studentPreference = studentPreference;
        this.id.setPreferenceId(studentPreference.getId());
    }

    public Course getCourse() {
        return course;
    }
}
