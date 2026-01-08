package com.example.lab4.controller;

import com.example.lab4.cqrs.command.CourseCommand;
import com.example.lab4.cqrs.command.CourseCommandHandler;
import com.example.lab4.cqrs.model.CourseReadModel;
import com.example.lab4.cqrs.query.CourseQueryService;
import com.example.lab4.dta.CreateCourseRequest;
import com.example.lab4.dta.UpdateCourseRequest;
import com.example.lab4.dto.CourseDetailDto;
import com.example.lab4.dto.StudentDefaultDto;
import com.example.lab4.service.CourseService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth")
)
@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

    @Autowired
    CourseService courseService;
    @Operation(
            summary = "Obține lista tuturor cursurilor",
            description = "Returnează un array cu toate cursurile disponibile în sistem."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de cursuri returnată cu succes",
            content = {
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseDetailDto.class)
                    ),
                    @Content(
                            mediaType = "application/xml",
                            schema = @Schema(implementation = CourseDetailDto.class)
                    )
            }
    )
    @ApiResponse(responseCode = "500", description = "Eroare internă a serverului")
    @GetMapping(produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE
    })
    public ResponseEntity<CourseDetailDto[]> getAll() throws Exception {
        CourseDetailDto[] resp;
        ResponseEntity< CourseDetailDto[]> responseEntity;
        resp = courseService.getList();
        responseEntity = ResponseEntity.ok(resp);
        return responseEntity;
    }

    public CourseRestController(CourseCommandHandler commandHandler, CourseQueryService queryService) {
        this.commandHandler = commandHandler;
        this.queryService = queryService;
    }

    private final CourseCommandHandler commandHandler;
    private final CourseQueryService queryService;

    @PostMapping("/commands/courses")
    @Operation(summary = "Create course (Command)",
            description = "Create a new course - writes to PostgreSQL and syncs to read model")
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody CreateCourseRequest request) {
        CourseCommand.CreateCourse command = new CourseCommand.CreateCourse(
                request.name(),
                request.code(),
                request.abbr(),
                request.type(),
                request.description(),
                request.capacity(),
                request.instructorId(),
                request.packId()
        );

        Long courseId = commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("message", "Course created successfully");
        response.put("storage", "PostgreSQL (primary) + ReadModel (secondary)");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/commands/courses/{courseId}")
    @Operation(summary = "Update course (Command)",
            description = "Update a course - writes to PostgreSQL and syncs to read model")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable Long courseId,
            @RequestBody UpdateCourseRequest request) {

        CourseCommand.UpdateCourse command = new CourseCommand.UpdateCourse(
                courseId,
                request.name(),
                request.description(),
                request.capacity()
        );

        commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("message", "Course updated successfully");

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/commands/courses/{courseId}")
    @Operation(summary = "Delete course (Command)",
            description = "Delete a course - removes from PostgreSQL and read model")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Long courseId) {
        CourseCommand.DeleteCourse command = new CourseCommand.DeleteCourse(courseId);
        commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("message", "Course deleted successfully");

        return ResponseEntity.ok(response);
    }
    @PostMapping("/commands/courses/{courseId}/enroll")
    @Operation(summary = "Enroll student (Command)",
            description = "Enroll a student in a course")
    public ResponseEntity<Map<String, Object>> enrollStudent(
            @PathVariable Long courseId,
            @RequestParam Long studentId) {

        CourseCommand.EnrollStudent command = new CourseCommand.EnrollStudent(courseId, studentId);
        commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("studentId", studentId);
        response.put("message", "Student enrolled successfully");

        return ResponseEntity.ok(response);
    }
    @PostMapping("/commands/courses/{courseId}/instructor")
    @Operation(summary = "Assign instructor (Command)",
            description = "Assign an instructor to a course")
    public ResponseEntity<Map<String, Object>> assignInstructor(
            @PathVariable Long courseId,
            @RequestParam Long instructorId) {

        CourseCommand.AssignInstructor command = new CourseCommand.AssignInstructor(courseId, instructorId);
        commandHandler.handle(command);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("instructorId", instructorId);
        response.put("message", "Instructor assigned successfully");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/queries/courses/{courseId}")
    @Operation(summary = "Get course (Query)",
            description = "Get a course from the read model (fast, denormalized)")
    public ResponseEntity<CourseReadModel> getCourse(@PathVariable Long courseId) {
        return queryService.getCourse(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all courses (Query).
     */
    @GetMapping("/queries/courses")
    @Operation(summary = "Get all courses (Query)",
            description = "Get all courses from the read model")
    public ResponseEntity<List<CourseReadModel>> getAllCourses() {
        return ResponseEntity.ok(queryService.getAllCourses());
    }

    /**
     * Get courses by type (Query).
     */
    @GetMapping("/queries/courses/type/{type}")
    @Operation(summary = "Get courses by type (Query)",
            description = "Get courses by type from indexed read model")
    public ResponseEntity<List<CourseReadModel>> getCoursesByType(@PathVariable String type) {
        return ResponseEntity.ok(queryService.getCoursesByType(type));
    }

    /**
     * Get courses by instructor (Query).
     */
    @GetMapping("/queries/courses/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor (Query)",
            description = "Get courses by instructor from indexed read model")
    public ResponseEntity<List<CourseReadModel>> getCoursesByInstructor(@PathVariable Long instructorId) {
        return ResponseEntity.ok(queryService.getCoursesByInstructor(instructorId));
    }

    /**
     * Get courses by pack (Query).
     */
    @GetMapping("/queries/courses/pack/{packId}")
    @Operation(summary = "Get courses by pack (Query)",
            description = "Get courses by pack from indexed read model")
    public ResponseEntity<List<CourseReadModel>> getCoursesByPack(@PathVariable Long packId) {
        return ResponseEntity.ok(queryService.getCoursesByPack(packId));
    }

    /**
     * Get available courses (Query).
     */
    @GetMapping("/queries/courses/available")
    @Operation(summary = "Get available courses (Query)",
            description = "Get courses with available capacity")
    public ResponseEntity<List<CourseReadModel>> getAvailableCourses() {
        return ResponseEntity.ok(queryService.getAvailableCourses());
    }

    /**
     * Search courses by name (Query).
     */
    @GetMapping("/queries/courses/search")
    @Operation(summary = "Search courses (Query)",
            description = "Search courses by name")
    public ResponseEntity<List<CourseReadModel>> searchCourses(@RequestParam String query) {
        return ResponseEntity.ok(queryService.searchCourses(query));
    }

    /**
     * Check if student is enrolled (Query).
     */
    @GetMapping("/queries/courses/{courseId}/enrolled/{studentId}")
    @Operation(summary = "Check enrollment (Query)",
            description = "Check if a student is enrolled in a course")
    public ResponseEntity<Map<String, Object>> isStudentEnrolled(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {

        boolean enrolled = queryService.isStudentEnrolled(courseId, studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("studentId", studentId);
        response.put("enrolled", enrolled);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Statistics Endpoints
    // =====================================================

    /**
     * Get CQRS statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get CQRS statistics",
            description = "Get statistics about the CQRS read model")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", queryService.getTotalCourseCount());
        stats.put("availableCourses", queryService.getAvailableCourseCount());
        stats.put("averageEnrollment", queryService.getAverageEnrollment());
        stats.put("writeStore", "PostgreSQL");
        stats.put("readStore", "In-Memory (simulating Redis)");

        return ResponseEntity.ok(stats);
    }

    /**
     * Refresh the read model from database.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh read model",
            description = "Rebuild the read model from the database")
    public ResponseEntity<Map<String, Object>> refreshReadModel() {
        queryService.refreshReadModel();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Read model refreshed successfully");
        response.put("courseCount", queryService.getTotalCourseCount());

        return ResponseEntity.ok(response);
    }

}
