package com.example.lab4.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

@Entity
public class ScheduleSaga {

    @Id
    private String sagaId;

    private Long packId;
    private String algorithm;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String beforeAssignmentsJson;   // snapshot (for compensation)

    @Column(columnDefinition = "TEXT")
    private String stableMatchRawResult;    // optional (debug)

    @Column(columnDefinition = "TEXT")
    private String error;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        STARTED,
        STABLEMATCH_OK,
        DB_APPLIED,
        COMPLETED,
        COMPENSATING,
        COMPENSATED,
        FAILED
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBeforeAssignmentsJson() {
        return beforeAssignmentsJson;
    }

    public void setBeforeAssignmentsJson(String beforeAssignmentsJson) {
        this.beforeAssignmentsJson = beforeAssignmentsJson;
    }

    public String getStableMatchRawResult() {
        return stableMatchRawResult;
    }

    public void setStableMatchRawResult(String stableMatchRawResult) {
        this.stableMatchRawResult = stableMatchRawResult;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}