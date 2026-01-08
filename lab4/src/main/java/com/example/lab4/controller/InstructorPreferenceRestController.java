package com.example.lab4.controller;

import com.example.lab4.dta.InstructorPreferenceRegisterDta;
import com.example.lab4.dto.preference.InstructorPreferenceDto;
import com.example.lab4.service.InstructorPreferenceService;
import com.example.lab4.service.InstructorService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth")
)
@RestController
@RequestMapping("/api/instructor")
public class InstructorPreferenceRestController {

    @Autowired
    private InstructorPreferenceService instructorPreferenceService;

    @Autowired
    private InstructorService instructorService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InstructorPreferenceDto>> getAllInstructorPreference(){
        List<InstructorPreferenceDto> instructorPreferences = instructorPreferenceService.getAll();
        return ResponseEntity.ok().body(instructorPreferences);
    }

    @GetMapping(value = "/pack/{pack_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InstructorPreferenceDto>> getInstructorPreferenceBasedOnPackId(@PathVariable("pack_id") Long packId){
        List<InstructorPreferenceDto> instructorPreferences = instructorPreferenceService.getByPack(packId);
        return ResponseEntity.ok().body(instructorPreferences);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void>  saveInstructorPreference(@RequestBody InstructorPreferenceRegisterDta[] instructorPreference) throws Exception{
        instructorPreferenceService.savePreference(instructorPreference);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/course/{code}")
    public ResponseEntity<Void> changeWeightForCourse(@PathVariable("code") String code, @Param("value") Long value) throws Exception{
        instructorPreferenceService.changeWeight(code, value);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/course/{code}")
    public ResponseEntity<Void> deleteInstructorPreference(@PathVariable("code") String code){
        instructorPreferenceService.deletePreferenceItem(code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteInstructorPreferenceById(@PathVariable("id") Long id){
        instructorPreferenceService.deletePreference(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/solve/{id}")
    public ResponseEntity<String> matchingStudentCourses(@PathVariable("id") Long packId, @RequestParam("alg") String alg) {
        String result = instructorService.matchPreference(packId, alg);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(value = "/solve")
    public ResponseEntity<String> matchingStudentCoursesForAllPacks(@RequestParam("alg") String alg) {
        String result = instructorService.matchPreferenceForAllPacks(alg);
        return ResponseEntity.ok().body(result);
    }
}
