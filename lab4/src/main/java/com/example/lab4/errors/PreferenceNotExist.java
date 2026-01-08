package com.example.lab4.errors;

public class PreferenceNotExist extends RuntimeException {
    String preferenceId;
    public PreferenceNotExist(String preferenceId) {
        super("preference not exist");
        this.preferenceId = preferenceId;
    }

    public String getPreferenceId() {
        return preferenceId;
    }
}
