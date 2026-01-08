package com.example.lab8;

import com.example.lab8.service.InMemoryMatchingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Lab8Config {

    @Bean
    public InMemoryMatchingStore inMemoryMatchingStore() {
        return new InMemoryMatchingStore();
    }

}
