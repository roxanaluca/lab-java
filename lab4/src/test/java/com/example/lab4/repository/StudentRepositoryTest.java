package com.example.lab4.repository;

import com.example.lab4.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Testcontainers(disabledWithoutDocker = true)
class StudentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("appdb")
            .withUsername("appuser")
            .withPassword("secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                String.format("jdbc:postgresql://%s:%d", postgres.getHost(), postgres.getFirstMappedPort()));
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.config.import", () -> "");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void repositoryCrudWorksWithMigrationSchema() {
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test.student@example.com");
        student.setPassword("secret");
        student.setCode("TS001");
        student.setYear(2025);

        Student saved = studentRepository.save(student);

        Student found = studentRepository.findByCode("TS001");
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());

        studentRepository.deleteByCode("TS001");
        assertFalse(studentRepository.existsByCode("TS001"));
    }
}
