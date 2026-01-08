package com.example.lab4.controller;

import com.example.lab4.dto.GradeEvent;
import com.example.lab4.entity.Grade;
import com.example.lab4.service.CSVGradeService;
import com.example.lab4.service.StudentGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/grades")
public class GradeRestController {

    @Autowired
    private CSVGradeService csvGradeService;

    @Autowired
    private StudentGradeService studentGradeService;

    @Operation(
            summary = "Upload grades CSV",
            description = "Uploads and processes a CSV file containing grades. " +
                    "Only users with the INSTRUCTOR or ADMIN role can access this endpoint.",
            security = {
                    @SecurityRequirement(name = "bearerAuth") // adjust name to your security scheme
            },
            requestBody = @RequestBody(
                    required = true,
                    description = "Multipart form containing the CSV file in the 'file' field",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV processed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Empty file or invalid input",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Empty file")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error while reading CSV",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Failed to read CSV: <error message>")
                    )
            )
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCsv(@RequestPart("file") MultipartFile file) {
        String message = "";
        try {
            message = csvGradeService.processFile(file);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Empty file");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read CSV: " + e.getMessage());
        }
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/student/{scode}/course/{ccode}")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<GradeEvent>> getGradesByStudentCodeAndCourseCode(@PathVariable String scode, @PathVariable String ccode) {
        List<GradeEvent> grades = studentGradeService.findGrades(scode, ccode);
        return ResponseEntity.ok(grades);
    }
}
