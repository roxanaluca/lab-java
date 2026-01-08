package com.example.lab4.cqrs.command;

import com.example.lab4.cqrs.command.CourseCommand.*;
import com.example.lab4.cqrs.model.CourseReadModel;
import com.example.lab4.cqrs.query.CourseReadModelStore;
import com.example.lab4.entity.Course;
import com.example.lab4.entity.Instructor;
import com.example.lab4.entity.Student;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.InstructorRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

/**
 * Command Handler for CQRS pattern.
 * Handles write operations to PostgreSQL and synchronizes to read model (Redis).
 */
@Service
public class CourseCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CourseCommandHandler.class);

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final PackRepository packRepository;
    private final CourseReadModelStore readModelStore;

    public CourseCommandHandler(CourseRepository courseRepository,
                                 StudentRepository studentRepository,
                                 InstructorRepository instructorRepository,
                                 PackRepository packRepository,
                                 CourseReadModelStore readModelStore) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.packRepository = packRepository;
        this.readModelStore = readModelStore;
    }

    @Transactional
    public Long handle(CreateCourse command) {
        logger.info("Handling CreateCourse command: {}", command.name());

        Course course = new Course();
        course.setName(command.name());
        course.setCode(command.code());
        course.setAbbr(command.abbr());
        course.setType(command.type());
        course.setDescription(command.description());
        course.setGroupCount(command.capacity());

        // Set instructor if provided
        if (command.instructorId() != null) {
            instructorRepository.findById(command.instructorId())
                    .ifPresent(course::setInstructor);
        }

        // Set pack if provided
        if (command.packId() != null) {
            packRepository.findById(command.packId())
                    .ifPresent(course::setPack);
        }

        Course saved = courseRepository.save(course);
        logger.info("Course created with ID: {}", saved.getId());

        // Sync to read model
        syncToReadModel(saved);

        return saved.getId();
    }

    @Transactional
    public void handle(UpdateCourse command) {
        logger.info("Handling UpdateCourse command for course: {}", command.courseId());

        Course course = courseRepository.findByIdWithStudents(command.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + command.courseId()));

        if (command.name() != null) {
            course.setName(command.name());
        }
        if (command.description() != null) {
            course.setDescription(command.description());
        }
        if (command.capacity() != null) {
            course.setGroupCount(command.capacity());
        }

        courseRepository.save(course);
        logger.info("Course {} updated", command.courseId());

        // Sync to read model
        syncToReadModel(course);
    }

    @Transactional
    public void handle(DeleteCourse command) {
        logger.info("Handling DeleteCourse command for course: {}", command.courseId());

        if (!courseRepository.existsById(command.courseId())) {
            throw new IllegalArgumentException("Course not found: " + command.courseId());
        }

        courseRepository.deleteById(command.courseId());
        logger.info("Course {} deleted", command.courseId());

        // Remove from read model
        readModelStore.delete(command.courseId());
    }

    @Transactional
    public void handle(EnrollStudent command) {
        logger.info("Handling EnrollStudent command: student {} in course {}",
                command.studentId(), command.courseId());

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + command.courseId()));

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + command.studentId()));


        if ( course.getStudents().size() >= course.getGroupCount()) {
            throw new IllegalStateException("Course is at full capacity");
        }

        course.getStudents().add(student);
        courseRepository.save(course);
        logger.info("Student {} enrolled in course {}", command.studentId(), command.courseId());

        // Sync to read model
        syncToReadModel(course);
    }

    /**
     * Handle UnenrollStudent command.
     */
    @Transactional
    public void handle(UnenrollStudent command) {
        logger.info("Handling UnenrollStudent command: student {} from course {}",
                command.studentId(), command.courseId());

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + command.courseId()));

        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + command.studentId()));

        if (course.getStudents() != null) {
            course.getStudents().remove(student);
            courseRepository.save(course);
            logger.info("Student {} unenrolled from course {}", command.studentId(), command.courseId());

            // Sync to read model
            syncToReadModel(course);
        }
    }

    @Transactional
    public void handle(AssignInstructor command) {
        logger.info("Handling AssignInstructor command: instructor {} to course {}",
                command.instructorId(), command.courseId());

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + command.courseId()));

        Instructor instructor = instructorRepository.findById(command.instructorId())
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found: " + command.instructorId()));

        course.setInstructor(instructor);
        courseRepository.save(course);
        logger.info("Instructor {} assigned to course {}", command.instructorId(), command.courseId());

        // Sync to read model
        syncToReadModel(course);
    }

    @Transactional
    public void handle(BatchUpdateCapacity command) {
        logger.info("Handling BatchUpdateCapacity command for {} courses",
                command.courseIds().size());

        for (Long courseId : command.courseIds()) {
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                course.setGroupCount(command.newCapacity());
                courseRepository.save(course);

                // Sync to read model
                syncToReadModel(course);
            }
        }

        logger.info("Batch capacity update completed");
    }


    private void syncToReadModel(Course course) {
        CourseReadModel readModel = CourseReadModel.fromEntity(course);
        readModelStore.save(readModel);
        logger.debug("Synced course {} to read model", course.getId());
    }
}
