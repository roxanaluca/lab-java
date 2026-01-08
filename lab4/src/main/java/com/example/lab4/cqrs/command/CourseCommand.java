package com.example.lab4.cqrs.command;

import java.util.List;

/**
 * Command objects for CQRS write operations on Courses.
 * Commands represent intent to change state.
 */
public sealed interface CourseCommand {

    /**
     * Command to create a new course.
     */
    record CreateCourse(
            String name,
            String code,
            String abbr,
            String type,
            String description,
            Integer capacity,
            Long instructorId,
            Long packId
    ) implements CourseCommand {}

    /**
     * Command to update a course.
     */
    record UpdateCourse(
            Long courseId,
            String name,
            String description,
            Integer capacity
    ) implements CourseCommand {}

    /**
     * Command to delete a course.
     */
    record DeleteCourse(Long courseId) implements CourseCommand {}

    /**
     * Command to enroll a student in a course.
     */
    record EnrollStudent(
            Long courseId,
            Long studentId
    ) implements CourseCommand {}

    /**
     * Command to remove a student from a course.
     */
    record UnenrollStudent(
            Long courseId,
            Long studentId
    ) implements CourseCommand {}

    /**
     * Command to assign an instructor to a course.
     */
    record AssignInstructor(
            Long courseId,
            Long instructorId
    ) implements CourseCommand {}

    /**
     * Command to batch update course capacities.
     */
    record BatchUpdateCapacity(
            List<Long> courseIds,
            Integer newCapacity
    ) implements CourseCommand {}
}
