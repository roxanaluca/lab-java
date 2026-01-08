package com.example.lab4.controller;

import com.example.lab4.dta.StudentChangeDetailsDta;
import com.example.lab4.dta.StudentRegisterDta;
import com.example.lab4.dto.PackDetailDto;
import com.example.lab4.dto.StudentDefaultDto;
import com.example.lab4.dto.StudentDetailDto;
import com.example.lab4.service.StudentService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth")
)
@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    @Autowired
    private StudentService studentService;

    @Operation(
            summary = "Obține detaliile unui student",
            description = "Returnează detaliile complete ale unui student identificat prin codul său unic."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Detalii student returnate cu succes",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentDefaultDto.class))
    )
    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDefaultDto> get(@PathVariable String code) throws Exception {
        StudentDefaultDto resp;
        ResponseEntity<StudentDefaultDto> responseEntity;
        resp = studentService.getDetails(code);
        resp.add(linkTo(methodOn(StudentRestController.class).getAllStudents()).withRel("students"),
                linkTo(methodOn(StudentRestController.class).register(null)).withRel("create"),
                linkTo(methodOn(StudentRestController.class).deleteStudent(code)).withRel("delete"),
                linkTo(methodOn(StudentRestController.class).update(code, null)).withRel("update"));
        responseEntity = ResponseEntity.ok(resp);
        return responseEntity;
    }

    @Operation(
            summary = "Înregistrează un student nou",
            description = "Creează un nou student și returnează id-ul creat."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Student creat cu succes",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentDefaultDto.class))
    )
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDefaultDto> register(@RequestBody StudentRegisterDta studentRegisterDta) throws Exception {
        StudentDefaultDto resp;
        ResponseEntity<StudentDefaultDto> responseEntity;
        resp = studentService.register(studentRegisterDta);
        resp.add(linkTo(methodOn(StudentRestController.class).getAllStudents()).withRel("students"),
                linkTo(methodOn(StudentRestController.class).get(studentRegisterDta.code())).withRel("details"),
                linkTo(methodOn(StudentRestController.class).update(studentRegisterDta.code(), null)).withRel("update"));
        responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(resp);
        return responseEntity;
    }

    @Operation(
            summary = "Actualizează detaliile unui student",
            description = "Modifică detaliile unui student existent și actualizează cursurile înscrise."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Student actualizat cu succes",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentDefaultDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Date invalide")
    @ApiResponse(responseCode = "404", description = "Student inexistent")
    @PutMapping("/{code}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDefaultDto> update(@PathVariable String code, @RequestBody StudentChangeDetailsDta studentChangeDetailsDta) throws Exception {
        StudentDefaultDto resp;
        ResponseEntity<StudentDefaultDto> responseEntity;
        resp = studentService.changeDetailsAndUpdateEnrollCourses(code, studentChangeDetailsDta);
        responseEntity = ResponseEntity.ok(resp);
        return responseEntity;
    }

    @Operation(
            summary = "Obține pachetele de cursuri ale unui student",
            description = "Returnează lista pachetelor de cursuri la care este înscris studentul specificat."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listă de pachete de cursuri returnată cu succes",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PackDetailDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Student inexistent")
    @GetMapping("/{code}/courses-packs")
    @PermitAll
    public ResponseEntity<PackDetailDto[]> getCoursesPacks(@PathVariable String code) throws Exception {
        PackDetailDto[] resp;
        ResponseEntity<PackDetailDto[]> responseEntity;
        resp = studentService.getEnrolledCourses(code);
        responseEntity = ResponseEntity.ok(resp);
        return responseEntity;
    }

    @Operation(
            summary = "Obține lista tuturor studenților",
            description = "Returnează o listă completă cu toți studenții existenți în sistem, împreună cu informațiile lor principale."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista studenților a fost returnată cu succes",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentDetailDto.class))
    )
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDefaultDto[]> getAllStudents() throws Exception {
        StudentDefaultDto[] resp;
        ResponseEntity<StudentDefaultDto[]> responseEntity;
        resp = studentService.getAll();
        responseEntity = ResponseEntity.ok(resp);
        return responseEntity;
    }

    @Operation(
            summary = "Șterge un student",
            description = "Șterge definitiv studentul identificat prin ID."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Studentul a fost șters cu succes"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Studentul nu a fost găsit"
    )
    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable String code) throws Exception {
        studentService.deleteOne(code);
        return ResponseEntity.noContent().build();
    }
}
