package com.example.lab8.dto;

import com.example.lab8.dta.Algorithm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SolveMatchingDto{

    String matchingId;
    String packId;
    Algorithm algorithm;
    Instant createdAt;
    List<AssignmentDto> assignments;
    List<String> unassignedStudents;
    String note;
    public SolveMatchingDto(String packId, Algorithm alg, String note) {
        this.packId = packId;
        this.algorithm = alg;
        createdAt = Instant.now();
        assignments = new ArrayList<>();
        unassignedStudents = new ArrayList<>();
        this.note = note;
        this.matchingId = "FALLBACK";
    }

    public SolveMatchingDto( String packId, Algorithm alg, List<AssignmentDto> assignments, List<String> unassignedStudents,  String note) {
        this.packId = packId;
        this.algorithm = alg;
        createdAt = Instant.now();
        this.assignments = assignments;
        this.unassignedStudents = unassignedStudents;
        this.note = note;
        this.matchingId = UUID.randomUUID().toString();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getMatchingId() {
        return matchingId;
    }
    public List<AssignmentDto> getAssignments() {
        return this.assignments;
    }
}