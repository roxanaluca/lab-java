package com.example.lab4.errors;

public class PackNotFound extends Exception{
    Long packId;
    public PackNotFound(Long packId) {
        super("Pack not found");
        this.packId = packId;
    }

    public Long getPackId() {
        return packId;
    }
}
