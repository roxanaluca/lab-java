package com.example.lab4.cqrs.model;

import com.example.lab4.entity.Course;
import com.example.lab4.entity.Student;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read model for Course in CQRS pattern.
 * Optimized for query operations, stored in Redis for fast access.
 * This is a denormalized view of the Course entity.
 */
public class CourseReadModel {

    private Long id;
    private String name;
    private String code;
    private String abbr;
    private String type;
    private String description;
    private Integer capacity;
    private int enrolledCount;
    private int availableSeats;
    private boolean isFull;

    // Denormalized instructor info
    private Long instructorId;
    private String instructorName;

    // Denormalized pack info
    private Long packId;
    private String packName;

    // Enrolled student IDs (for quick lookup)
    private List<Long> enrolledStudentIds;

    // Metadata
    private Instant lastUpdated;

    public CourseReadModel() {
        this.lastUpdated = Instant.now();
    }

    /**
     * Create a read model from a Course entity.
     */
    public static CourseReadModel fromEntity(Course course) {
        CourseReadModel model = new CourseReadModel();
        model.setId(course.getId());
        model.setName(course.getName());
        model.setCode(course.getCode());
        model.setAbbr(course.getAbbr());
        model.setType(course.getType());
        model.setDescription(course.getDescription());
        model.setCapacity(course.getGroupCount());

        // Calculate enrollment stats
        int enrolled = course.getStudents() != null && !course.getStudents().isEmpty() ? course.getStudents().size() : 0;
        model.setEnrolledCount(enrolled);

        int capacity = course.getGroupCount() != 0 ? 10 : Integer.MAX_VALUE;
        model.setAvailableSeats(Math.max(0, capacity - enrolled));
        model.setFull(enrolled >= capacity);

        // Denormalize instructor info
        if (course.getInstructor() != null) {
            model.setInstructorId(course.getInstructor().getId());
            model.setInstructorName(course.getInstructor().getName());
        }

        // Denormalize pack info
        if (course.getPack() != null) {
            model.setPackId(course.getPack().getId());
            model.setPackName(course.getPack().getName());
        }

        // Store enrolled student IDs
        if (course.getStudents() != null) {
            model.setEnrolledStudentIds(
                    course.getStudents().stream()
                            .map(Student::getId)
                            .collect(Collectors.toList())
            );
        } else {
            model.setEnrolledStudentIds(List.of());
        }

        model.setLastUpdated(Instant.now());
        return model;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getAbbr() { return abbr; }
    public void setAbbr(String abbr) { this.abbr = abbr; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public boolean isFull() { return isFull; }
    public void setFull(boolean full) { isFull = full; }

    public Long getInstructorId() { return instructorId; }
    public void setInstructorId(Long instructorId) { this.instructorId = instructorId; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public Long getPackId() { return packId; }
    public void setPackId(Long packId) { this.packId = packId; }

    public String getPackName() { return packName; }
    public void setPackName(String packName) { this.packName = packName; }

    public List<Long> getEnrolledStudentIds() { return enrolledStudentIds; }
    public void setEnrolledStudentIds(List<Long> enrolledStudentIds) {
        this.enrolledStudentIds = enrolledStudentIds;
    }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    /**
     * Check if a student is enrolled in this course.
     */
    public boolean isStudentEnrolled(Long studentId) {
        return enrolledStudentIds != null && enrolledStudentIds.contains(studentId);
    }
}
