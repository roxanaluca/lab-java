package com.example.lab4.cqrs.query;

import com.example.lab4.cqrs.model.CourseReadModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Read Model Store for CQRS pattern - implements polyglot persistence.
 * In production, this would be backed by Redis for fast queries.
 * Currently uses in-memory storage as a demonstration.
 *
 * The read model store provides:
 * - Fast lookups by ID
 * - Indexed queries (by type, instructor, pack)
 * - Full-text search capabilities
 */
@Component
public class CourseReadModelStore {

    private static final Logger logger = LoggerFactory.getLogger(CourseReadModelStore.class);

    // Primary storage (simulating Redis key-value store)
    private final Map<Long, CourseReadModel> courseStore = new ConcurrentHashMap<>();

    // Secondary indexes (simulating Redis sets/sorted sets)
    private final Map<String, Set<Long>> coursesByType = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> coursesByInstructor = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> coursesByPack = new ConcurrentHashMap<>();
    private final Set<Long> availableCourses = ConcurrentHashMap.newKeySet();

    /**
     * Save or update a course in the read model.
     */
    public void save(CourseReadModel course) {
        Long id = course.getId();

        // Remove from old indexes if updating
        CourseReadModel existing = courseStore.get(id);
        if (existing != null) {
            removeFromIndexes(existing);
        }

        // Store the course
        courseStore.put(id, course);

        // Update indexes
        addToIndexes(course);

        logger.debug("Saved course {} to read model store", id);
    }

    /**
     * Delete a course from the read model.
     */
    public void delete(Long courseId) {
        CourseReadModel course = courseStore.remove(courseId);
        if (course != null) {
            removeFromIndexes(course);
            logger.debug("Deleted course {} from read model store", courseId);
        }
    }

    /**
     * Get a course by ID.
     */
    public Optional<CourseReadModel> findById(Long courseId) {
        return Optional.ofNullable(courseStore.get(courseId));
    }

    /**
     * Get all courses.
     */
    public List<CourseReadModel> findAll() {
        return new ArrayList<>(courseStore.values());
    }

    /**
     * Get courses by type.
     */
    public List<CourseReadModel> findByType(String type) {
        Set<Long> ids = coursesByType.getOrDefault(type, Set.of());
        return ids.stream()
                .map(courseStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by instructor.
     */
    public List<CourseReadModel> findByInstructor(Long instructorId) {
        Set<Long> ids = coursesByInstructor.getOrDefault(instructorId, Set.of());
        return ids.stream()
                .map(courseStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by pack.
     */
    public List<CourseReadModel> findByPack(Long packId) {
        Set<Long> ids = coursesByPack.getOrDefault(packId, Set.of());
        return ids.stream()
                .map(courseStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get all available (not full) courses.
     */
    public List<CourseReadModel> findAvailable() {
        return availableCourses.stream()
                .map(courseStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Search courses by name (simple contains search).
     */
    public List<CourseReadModel> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return courseStore.values().stream()
                .filter(c -> c.getName() != null &&
                        c.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Get courses with available seats.
     */
    public List<CourseReadModel> findWithAvailableSeats(int minSeats) {
        return courseStore.values().stream()
                .filter(c -> c.getAvailableSeats() >= minSeats)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by multiple IDs (batch lookup).
     */
    public List<CourseReadModel> findByIds(List<Long> courseIds) {
        return courseIds.stream()
                .map(courseStore::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Check if a course exists.
     */
    public boolean exists(Long courseId) {
        return courseStore.containsKey(courseId);
    }

    /**
     * Get total count of courses.
     */
    public int count() {
        return courseStore.size();
    }

    /**
     * Clear all data (for testing).
     */
    public void clear() {
        courseStore.clear();
        coursesByType.clear();
        coursesByInstructor.clear();
        coursesByPack.clear();
        availableCourses.clear();
        logger.info("Read model store cleared");
    }

    /**
     * Rebuild indexes from stored data.
     */
    public void rebuildIndexes() {
        coursesByType.clear();
        coursesByInstructor.clear();
        coursesByPack.clear();
        availableCourses.clear();

        for (CourseReadModel course : courseStore.values()) {
            addToIndexes(course);
        }

        logger.info("Indexes rebuilt for {} courses", courseStore.size());
    }

    // Private helper methods

    private void addToIndexes(CourseReadModel course) {
        Long id = course.getId();

        // Index by type
        if (course.getType() != null) {
            coursesByType.computeIfAbsent(course.getType(), k -> ConcurrentHashMap.newKeySet())
                    .add(id);
        }

        // Index by instructor
        if (course.getInstructorId() != null) {
            coursesByInstructor.computeIfAbsent(course.getInstructorId(), k -> ConcurrentHashMap.newKeySet())
                    .add(id);
        }

        // Index by pack
        if (course.getPackId() != null) {
            coursesByPack.computeIfAbsent(course.getPackId(), k -> ConcurrentHashMap.newKeySet())
                    .add(id);
        }

        // Index available courses
        if (!course.isFull()) {
            availableCourses.add(id);
        }
    }

    private void removeFromIndexes(CourseReadModel course) {
        Long id = course.getId();

        // Remove from type index
        if (course.getType() != null) {
            Set<Long> typeSet = coursesByType.get(course.getType());
            if (typeSet != null) typeSet.remove(id);
        }

        // Remove from instructor index
        if (course.getInstructorId() != null) {
            Set<Long> instructorSet = coursesByInstructor.get(course.getInstructorId());
            if (instructorSet != null) instructorSet.remove(id);
        }

        // Remove from pack index
        if (course.getPackId() != null) {
            Set<Long> packSet = coursesByPack.get(course.getPackId());
            if (packSet != null) packSet.remove(id);
        }

        // Remove from available courses
        availableCourses.remove(id);
    }
}
