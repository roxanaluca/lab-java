package com.example.lab4.errors;

public class PackTimeout extends RuntimeException {
    Long packId;
    public PackTimeout(Long packId) {
        super("Pack has reach timeout");
        this.packId = packId;
    }

    public Long getPackId() {
        return packId;
    }
}
