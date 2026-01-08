package com.example.lab4.controller;

import com.example.lab4.errors.CourseDuplicate;
import com.example.lab4.errors.CourseNotFound;
import com.example.lab4.errors.CourseOutOfLimit;
import com.example.lab4.errors.PackNotFound;
import com.example.lab4.errors.PreferenceNotExist;
import com.example.lab4.errors.StudentNotFound;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.postgresql.util.PSQLException;

import java.util.Map;

@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StudentNotFound.class)
    public ResponseEntity<Map<String, String>> handleStudentNotFound(StudentNotFound ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "student_id", ex.getStudentCode());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseNotFound.class)
    public ResponseEntity<Map<String, String>> handleCourseNotFound(CourseNotFound ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "course_id", ex.getCourseCode()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseOutOfLimit.class)
    public ResponseEntity<Map<String, String>> handleCourseOutOfLimit(CourseOutOfLimit ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "course_id", ex.getCourseCode()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PackNotFound.class)
    public ResponseEntity<Map<String, String>> handlePackNotFound(PackNotFound ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "course_id", ex.getPackId().toString()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CourseDuplicate.class)
    public ResponseEntity<Map<String, String>> handleCourseOutOfLimit(CourseDuplicate ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "course_id", ex.getCourseCode()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    private Throwable getRootCause(Throwable t) {
        Throwable r = t;
        while (r.getCause() != null && r.getCause() != r) {
            r = r.getCause();
        }
        return r;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "500"
        );
        Throwable root = getRootCause(ex);

        if (root instanceof PSQLException psqlEx &&
                "23505".equals(psqlEx.getSQLState())) {

            assert psqlEx.getServerErrorMessage() != null;
            if ("students_code_key".equals(psqlEx.getServerErrorMessage().getConstraint())) {
                body = Map.of(
                        "error", "Codul studentului este deja folosit",
                        "status", "409"
                );
                return new ResponseEntity<>(body, HttpStatus.CONFLICT);
            }
            if ("students_email_key".equals(psqlEx.getServerErrorMessage().getConstraint())) {
                body = Map.of(
                        "error", "Email-ul este deja folosit",
                        "status", "409"
                );
                return new ResponseEntity<>(body, HttpStatus.CONFLICT);
            }
        }

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> body = Map.of("error", ex.getMessage(),
                "status", "400");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404"
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PreferenceNotExist.class)
    public ResponseEntity<Map<String, String>> handlePreferenceNotExist(PreferenceNotExist ex) {
        Map<String, String> body = Map.of(
                "error", ex.getMessage(),
                "status", "404",
                "preference_id", ex.getPreferenceId().toString()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
