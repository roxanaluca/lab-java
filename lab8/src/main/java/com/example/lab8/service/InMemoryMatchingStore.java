package com.example.lab8.service;

import com.example.lab8.dto.SolveMatchingDto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMatchingStore {
    private final Map<String, SolveMatchingDto> byId = new ConcurrentHashMap<>();

    public void put(SolveMatchingDto response) {
        byId.put(response.getMatchingId(), response);
    }

    public Optional<SolveMatchingDto> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<SolveMatchingDto> listAll() {
        return byId.values().stream()
                .sorted(Comparator.comparing(SolveMatchingDto::getCreatedAt).reversed())
                .toList();
    }
}