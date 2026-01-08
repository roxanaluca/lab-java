package com.example.lab4.controller;

import com.example.lab4.service.ScheduleSagaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saga")
public class ScheduleSagaController {

    private final ScheduleSagaService sagaService;

    public ScheduleSagaController(ScheduleSagaService sagaService) {
        this.sagaService = sagaService;
    }

    @PostMapping("/schedule/{packId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String start(@PathVariable Long packId, @RequestParam String algorithm) {
        return sagaService.startScheduleSaga(packId, algorithm);
    }
}
