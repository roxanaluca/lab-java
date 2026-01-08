package com.example.lab8.controllers;

import com.example.lab8.dta.SolveMatchingDta;
import com.example.lab8.dto.AssignmentDto;
import com.example.lab8.dto.SolveMatchingDto;
import com.example.lab8.service.MatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
public class Controller {
    private final MatchingService matchingService;

    public Controller(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/solve")
    @ResponseStatus(HttpStatus.CREATED)
    public SolveMatchingDto solve(@RequestBody SolveMatchingDta req) {
        return matchingService.solve(req);
    }

    @GetMapping
    public List<SolveMatchingDto> listAll() {
        return matchingService.listAll();
    }

    @GetMapping("/{matchingId}")
    public SolveMatchingDto getOne(@PathVariable String matchingId) {
        return matchingService.getByIdOrThrow(matchingId);
    }

    @GetMapping("/{matchingId}/students/{studentId}")
    public AssignmentDto getForStudent(@PathVariable String matchingId, @PathVariable String studentId) {
        return matchingService.getAssignmentForStudent(matchingId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not assigned: " + studentId));
    }

    @GetMapping("/{matchingId}/courses/{courseId}")
    public List<String> getForCourse(@PathVariable String matchingId, @PathVariable String courseId) {
        return matchingService.getStudentsForCourse(matchingId, courseId);
    }
}
