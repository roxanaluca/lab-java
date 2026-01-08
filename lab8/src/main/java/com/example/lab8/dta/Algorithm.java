package com.example.lab8.dta;

public enum Algorithm {
    RANDOM("RANDOM"),
    GALE_SHAPLEY("GALE_SHAPLEY");
    private final String value;

    Algorithm(String value) {
        this.value = value;
    }
}