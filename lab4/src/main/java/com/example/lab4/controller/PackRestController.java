package com.example.lab4.controller;


import com.example.lab4.dto.PackDefaultDto;
import com.example.lab4.entity.Pack;
import com.example.lab4.service.PackService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@OpenAPIDefinition(
        security = @SecurityRequirement(name = "bearerAuth")
)
@RestController
@RequestMapping("/api/packs")
public class PackRestController {
    @Autowired
    private PackService packService;

    @Operation(
            summary = "Obține detaliile unui pachet de cursuri",
            description = "Returnează informațiile despre un pachet identificat prin ID-ul său unic."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pachet găsit și returnat cu succes",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PackDefaultDto.class)),
            headers = {
                    @Header(
                            name = "ETag",
                            description = "Entity tag of the resource",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @Parameter(
            name = "If-None-Match",
            description = "ETag value from a previous response",
            in = ParameterIn.HEADER,
            required = false,
            schema = @Schema(type = "string")
    )
    @GetMapping("/{id}")
    @PermitAll
    public PackDefaultDto getPackService(@PathVariable Long id) {
        return packService.getPackById(id);
    }

    @GetMapping
    @PermitAll
    public  PackDefaultDto[] getAllPacks() {
        return packService.getAllPacks();
    }
}
