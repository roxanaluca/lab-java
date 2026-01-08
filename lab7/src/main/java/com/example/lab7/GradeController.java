package com.example.lab7;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeEventPublisher gradeEventPublisher;

    public GradeController(GradeEventPublisher gradeEventPublisher) {
        this.gradeEventPublisher = gradeEventPublisher;
    }

    @PostMapping
    public ResponseEntity<Void> publishGrade(@RequestBody GradeRequest request) {
        gradeEventPublisher.publishGradeAssigned(
                request.getStudentCode(),
                request.getCourseCode(),
                request.getGrade()
        );
        return ResponseEntity.accepted().build();
    }
}