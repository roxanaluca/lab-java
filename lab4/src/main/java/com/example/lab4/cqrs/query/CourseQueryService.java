package com.example.lab4.cqrs.query;

import com.example.lab4.cqrs.model.CourseReadModel;
import com.example.lab4.entity.Course;
import com.example.lab4.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Query Service for CQRS pattern.
 * Handles all read operations using the optimized read model store.
 * Provides fast queries without touching the primary database (PostgreSQL).
 */
@Service
public class CourseQueryService {

    private static final Logger logger = LoggerFactory.getLogger(CourseQueryService.class);

    private final CourseReadModelStore readModelStore;
    private final CourseRepository courseRepository;

    public CourseQueryService(CourseReadModelStore readModelStore, CourseRepository courseRepository) {
        this.readModelStore = readModelStore;
        this.courseRepository = courseRepository;
    }

    /**
     * Initialize the read model from the database on startup.
     */
    @PostConstruct
    public void initializeReadModel() {
        logger.info("Initializing CQRS read model from database...");

        List<Course> allCourses = courseRepository.findAllWithStudents();
        for (Course course : allCourses) {
            CourseReadModel readModel = CourseReadModel.fromEntity(course);
            readModelStore.save(readModel);
        }

        logger.info("CQRS read model initialized with {} courses", allCourses.size());
    }

    // =====================================================
    // Basic Query Operations
    // =====================================================

    /**
     * Get a course by ID (from read model).
     */
    public Optional<CourseReadModel> getCourse(Long courseId) {
        logger.debug("Query: getCourse({})", courseId);
        return readModelStore.findById(courseId);
    }

    /**
     * Get all courses.
     */
    public List<CourseReadModel> getAllCourses() {
        logger.debug("Query: getAllCourses()");
        return readModelStore.findAll();
    }

    /**
     * Get courses by multiple IDs.
     */
    public List<CourseReadModel> getCoursesByIds(List<Long> courseIds) {
        logger.debug("Query: getCoursesByIds({})", courseIds.size());
        return readModelStore.findByIds(courseIds);
    }

    // =====================================================
    // Indexed Query Operations
    // =====================================================

    /**
     * Get courses by type (e.g., "optional", "mandatory").
     */
    public List<CourseReadModel> getCoursesByType(String type) {
        logger.debug("Query: getCoursesByType({})", type);
        return readModelStore.findByType(type);
    }

    /**
     * Get courses by instructor.
     */
    public List<CourseReadModel> getCoursesByInstructor(Long instructorId) {
        logger.debug("Query: getCoursesByInstructor({})", instructorId);
        return readModelStore.findByInstructor(instructorId);
    }

    /**
     * Get courses by pack.
     */
    public List<CourseReadModel> getCoursesByPack(Long packId) {
        logger.debug("Query: getCoursesByPack({})", packId);
        return readModelStore.findByPack(packId);
    }

    // =====================================================
    // Specialized Query Operations
    // =====================================================

    /**
     * Get all available (not full) courses.
     */
    public List<CourseReadModel> getAvailableCourses() {
        logger.debug("Query: getAvailableCourses()");
        return readModelStore.findAvailable();
    }

    /**
     * Get courses with a minimum number of available seats.
     */
    public List<CourseReadModel> getCoursesWithAvailableSeats(int minSeats) {
        logger.debug("Query: getCoursesWithAvailableSeats({})", minSeats);
        return readModelStore.findWithAvailableSeats(minSeats);
    }

    /**
     * Search courses by name.
     */
    public List<CourseReadModel> searchCourses(String query) {
        logger.debug("Query: searchCourses({})", query);
        return readModelStore.searchByName(query);
    }

    /**
     * Check if a student is enrolled in a course.
     */
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        logger.debug("Query: isStudentEnrolled({}, {})", courseId, studentId);
        return readModelStore.findById(courseId)
                .map(c -> c.isStudentEnrolled(studentId))
                .orElse(false);
    }

    /**
     * Get enrollment count for a course.
     */
    public int getEnrollmentCount(Long courseId) {
        logger.debug("Query: getEnrollmentCount({})", courseId);
        return readModelStore.findById(courseId)
                .map(CourseReadModel::getEnrolledCount)
                .orElse(0);
    }

    /**
     * Check if a course has available capacity.
     */
    public boolean hasCapacity(Long courseId) {
        logger.debug("Query: hasCapacity({})", courseId);
        return readModelStore.findById(courseId)
                .map(c -> !c.isFull())
                .orElse(false);
    }

    // =====================================================
    // Statistics and Aggregations
    // =====================================================

    /**
     * Get total course count.
     */
    public int getTotalCourseCount() {
        return readModelStore.count();
    }

    /**
     * Get count of available courses.
     */
    public int getAvailableCourseCount() {
        return readModelStore.findAvailable().size();
    }

    /**
     * Get average enrollment across all courses.
     */
    public double getAverageEnrollment() {
        List<CourseReadModel> all = readModelStore.findAll();
        if (all.isEmpty()) return 0;

        return all.stream()
                .mapToInt(CourseReadModel::getEnrolledCount)
                .average()
                .orElse(0);
    }

    // =====================================================
    // Maintenance Operations
    // =====================================================

    /**
     * Refresh the read model from the database.
     * Call this if the read model gets out of sync.
     */
    public void refreshReadModel() {
        logger.info("Refreshing CQRS read model from database...");
        readModelStore.clear();
        initializeReadModel();
    }

    /**
     * Check if a course exists in the read model.
     */
    public boolean exists(Long courseId) {
        return readModelStore.exists(courseId);
    }
}
