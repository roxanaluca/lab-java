package com.example.lab4.controller;

import com.example.lab4.dta.StudentPreferenceCourseRegisterDta;
import com.example.lab4.dta.StudentPreferenceRegisterDta;
import com.example.lab4.dto.PackDefaultDto;
import com.example.lab4.dto.PackDetailDto;
import com.example.lab4.dto.preference.StudentPreferenceDefaultDto;
import com.example.lab4.service.StudentPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
@Tag(name = "Preferințe", description = "Operațiuni pentru gestionarea preferințelor studenților")
@RestController
@RequestMapping("/api/preference")
public class StudentPreferenceController {

    @Autowired
    private StudentPreferenceService studentPreferenceService;

    @Operation(
            summary = "Înregistrează preferința unui student",
            description = "Permite unui student să își înregistreze preferințele pentru cursuri."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Preferință creată cu succes"),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide"),
            @ApiResponse(responseCode = "401", description = "Neautorizat")
    })
    @PostMapping(value = "/submit")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<Void> submit(String studentCode) throws Exception {
        String resp;
        resp = studentPreferenceService.submitPreference(studentCode);
        URI location = linkTo(methodOn(StudentPreferenceController.class)
                .viewSubmitted(Long.valueOf(resp)))
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<StudentPreferenceDefaultDto> register( @RequestBody StudentPreferenceRegisterDta studentPreferenceRegisterDta) throws Exception {
        StudentPreferenceDefaultDto resp;
        ResponseEntity<StudentPreferenceDefaultDto> responseEntity;
        resp = studentPreferenceService.register(studentPreferenceRegisterDta);
        resp.add(linkTo(methodOn(StudentPreferenceController.class)
                .viewUnSubmitted(resp.getPreferenceId()))
                .withSelfRel());
        responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(resp);
        return responseEntity;
    }


    @Operation(
            summary = "Vizualizează o preferință după ID",
            description = "Returnează detaliile unei preferințe specifice."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferința a fost găsită",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PackDefaultDto.class)))),
            @ApiResponse(responseCode = "404", description = "Preferința nu există")
    })
    @GetMapping("/submitted/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<List<PackDefaultDto>>> viewSubmitted(@PathVariable Long id) throws Exception {
        PackDefaultDto[] resp;
        ResponseEntity<CollectionModel<List<PackDefaultDto>>> responseEntity;
        resp = studentPreferenceService.viewSubmittedPreference(id);

        List<PackDefaultDto> respList = Arrays.stream(resp).map(pack -> {
            if (pack.getPackId() != 0L)
                pack.add(
                    linkTo(methodOn(PackRestController.class).getPackService(id)).withSelfRel());
            return pack;
        }).toList();
        CollectionModel<List<PackDefaultDto>> collectionModel =
                CollectionModel.of(Collections.singleton(respList));

        String studentCode = studentPreferenceService.getSubmittedStudentCode(id);
        collectionModel.add(linkTo(methodOn(StudentPreferenceController.class).delete(id)).withSelfRel(),
                linkTo(methodOn(StudentPreferenceController.class).viewStudentPreference(studentCode)).withSelfRel(),
                linkTo(methodOn(StudentPreferenceController.class).viewHistory(studentCode)).withSelfRel());
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(collectionModel);
        return responseEntity;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<List<PackDefaultDto>>> viewUnSubmitted(@PathVariable String id) throws Exception {
        PackDefaultDto[] resp;
        ResponseEntity<CollectionModel<List<PackDefaultDto>>> responseEntity;
        resp = studentPreferenceService.viewUnsubmittedPreference(id);

        List<PackDefaultDto> respList = Arrays.stream(resp).map(pack -> {
            if (pack.getPackId() != 0L)
                pack.add(
                        linkTo(methodOn(PackRestController.class).getPackService(pack.getPackId())).withSelfRel());
            return pack;
        }).toList();
        CollectionModel<List<PackDefaultDto>> collectionModel =
                CollectionModel.of(Collections.singleton(respList));

        String studentCode = studentPreferenceService.getUnSubmittedStudentCode(id);
        collectionModel.add(
                linkTo(methodOn(StudentPreferenceController.class).viewStudentPreference(studentCode)).withSelfRel(),
                linkTo(methodOn(StudentPreferenceController.class).viewHistory(studentCode)).withSelfRel());
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(collectionModel);
        return responseEntity;
    }


    @Operation(
            summary = "Vizualizează preferințele unui student",
            description = "Returnează preferințele active ale studentului identificat prin cod."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferințele au fost returnate",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PackDefaultDto.class)))),
            @ApiResponse(responseCode = "404", description = "Studentul nu a fost găsit")
    })
    @GetMapping("/submitted/student/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackDefaultDto[]> viewStudentPreference(@PathVariable String code) throws Exception {
        PackDefaultDto[] resp;
        ResponseEntity<PackDefaultDto[]> responseEntity;
        resp = studentPreferenceService.viewSubmittedByStudentCode(code);
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(resp);
        return responseEntity;
    }


    @Operation(
            summary = "Vizualizează istoricul preferințelor",
            description = "Returnează toate versiunile istorice ale preferințelor unui student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Istoricul a fost returnat",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StudentPreferenceDefaultDto.class)))),
            @ApiResponse(responseCode = "404", description = "Studentul nu a fost găsit")
    })
    @GetMapping("/submitted/student/{code}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentPreferenceDefaultDto[]> viewHistory(@PathVariable String code) throws Exception {
        StudentPreferenceDefaultDto[] resp;
        ResponseEntity<StudentPreferenceDefaultDto[]> responseEntity;
        resp = studentPreferenceService.viewAllHistory(code);
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(resp);
        return responseEntity;
    }

    @GetMapping("/student/{code}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PackDetailDto[]>> viewUnsubmittedHistory(@PathVariable String code) throws Exception {
        List<PackDetailDto[]> resp;
        ResponseEntity<List<PackDetailDto[]>> responseEntity;
        resp = studentPreferenceService.viewHistory(code);
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(resp);
        return responseEntity;
    }


    @Operation(
            summary = "Actualizează un element de preferință",
            description = "Permite actualizarea unui modul specific din preferința unui student."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferință actualizată",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentPreferenceDefaultDto.class))),
            @ApiResponse(responseCode = "404", description = "Preferința sau elementul nu există")
    })
    @PutMapping(value = "/submitted/{id}/preferenceItem/", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentPreferenceDefaultDto> updatePreferenceItem(@PathVariable Long id, @RequestBody StudentPreferenceCourseRegisterDta preferenceRegisterDta) throws Exception {
        StudentPreferenceDefaultDto resp;
        ResponseEntity<StudentPreferenceDefaultDto> responseEntity;
        resp = studentPreferenceService.addSubmittedPreferenceItem(id, preferenceRegisterDta);
        responseEntity = ResponseEntity.status(HttpStatus.OK).body(resp);
        return responseEntity;
    }

    @PutMapping(value = "/preferenceItem/student/{id}", consumes =  MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<StudentPreferenceDefaultDto> updateUnsubmittedPreferenceItem(@PathVariable String id, @RequestBody StudentPreferenceCourseRegisterDta preferenceRegisterDta) throws Exception {
        StudentPreferenceDefaultDto resp;
        ResponseEntity<StudentPreferenceDefaultDto> responseEntity;
        studentPreferenceService.addCourseUnsubmitted(id, preferenceRegisterDta.courseCode(),preferenceRegisterDta.position(), preferenceRegisterDta.packId());
        responseEntity = ResponseEntity.ok().build();
        return responseEntity;
    }


    @Operation(
            summary = "Șterge o preferință",
            description = "Permite administratorului să șteargă o preferință după ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Preferința a fost ștearsă"),
            @ApiResponse(responseCode = "404", description = "Preferința nu a fost găsită")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        studentPreferenceService.deleteSubmittedPreference(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/course/{cid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @PathVariable String course) throws Exception {
        studentPreferenceService.deleteSubmittedPreference(id);
        return ResponseEntity.noContent().build();
    }

}
