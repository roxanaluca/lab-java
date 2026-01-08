package com.example.lab4.dto.preference;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Algorithm {
    RANDOM("RANDOM"),
    GALE_SHAPLEY("GALE_SHAPLEY");
    private final String value;

    Algorithm(String value) {
        this.value = value;
    }
    @JsonValue
    public String getValue() {
        return value;
    }
}