package com.example.lab4.controller;

import com.example.lab4.dto.StudentDetailDto;
import com.example.lab4.errors.StudentNotFound;
import com.example.lab4.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentRestControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = Mockito.mock(StudentService.class);
        StudentRestController controller = new StudentRestController();
        ReflectionTestUtils.setField(controller, "studentService", studentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getStudent_returnsDetails() throws Exception {
        StudentDetailDto dto = new StudentDetailDto();
        dto.setStudentId(42L);

        when(studentService.getDetails("abc")).thenReturn(dto);

        mockMvc.perform(get("/api/students/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(42L));
    }

    @Test
    void getStudent_missingReturnsNotFound() throws Exception {
        when(studentService.getDetails("missing")).thenThrow(new StudentNotFound("missing"));

        mockMvc.perform(get("/api/students/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.student_id").value("missing"));
    }
}
