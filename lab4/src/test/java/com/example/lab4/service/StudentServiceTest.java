package com.example.lab4.service;

import com.example.lab4.dta.StudentChangeDetailsDta;
import com.example.lab4.dta.StudentRegisterDta;
import com.example.lab4.dto.StudentDefaultDto;
import com.example.lab4.entity.Student;
import com.example.lab4.errors.StudentNotFound;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.CourseStudentRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseStudentRepository courseStudentRepository;

    @Mock
    private PackRepository packRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void register_encodesPasswordAndReturnsId() throws Exception {
        StudentRegisterDta dta = new StudentRegisterDta("Student", 2025, "ST1", "student@example.com", "secret");

        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        StudentDefaultDto result = studentService.register(dta);

        assertEquals(99L, result.getStudentId());
        verify(passwordEncoder).encode("secret");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void changeDetailsAndUpdateEnrollCourses_missingStudentThrows() {
        when(studentRepository.findByCode("missing")).thenReturn(null);

        StudentChangeDetailsDta changeDetails = new StudentChangeDetailsDta(2025, new String[]{}, "missing@example.com");

        assertThrows(StudentNotFound.class, () -> studentService.changeDetailsAndUpdateEnrollCourses("missing", changeDetails));
    }
}
