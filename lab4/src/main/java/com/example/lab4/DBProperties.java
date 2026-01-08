package com.example.lab4;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record DBProperties(
        @Value("${spring.datasource.url}") String url,
        @Nullable @Value("${spring.datasource.username}") String username,
        @Nullable @Value("${spring.datasource.password}") String password,
        @Value("${spring.datasource.driver-class-name}") String driverClassName
) {}