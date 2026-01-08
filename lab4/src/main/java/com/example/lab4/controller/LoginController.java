package com.example.lab4.controller;

import com.example.lab4.dto.PackDefaultDto;
import com.example.lab4.webfilter.DBUserDetailsService;
import com.example.lab4.webfilter.JwtService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.media.Schema.AdditionalPropertiesValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/web")
public class LoginController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(AuthenticationManager authManager, UserDetailsService dbUserDetailsService, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.userDetailsService = dbUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private JwtService jwtService;


    @Hidden
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);
        UserDetails userDetails = (UserDetails) authRequest.getPrincipal();

        try {
            Authentication auth = authManager.authenticate(authRequest);
            String token = jwtService.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "user", auth.getName(),
                    "roles", auth.getAuthorities().toString(),
                    "token", token
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Invalid credentials"));
        }
    }

    @Operation(
            summary = "Login and retrieve a JWT token",
            description = "Authenticates the user using username and password, and returns a JWT token for future authorized requests.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    additionalProperties = AdditionalPropertiesValue.TRUE,
                                    example = """
                                            {
                                              "username": "admin",
                                              "password": "qwerty1234"
                                            }
                                            """
                            )

                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    additionalProperties = AdditionalPropertiesValue.TRUE,
                                    example = """
                                              {
                                              "status": "ok",
                                              "user": "admin",
                                              "roles": "[ADMIN]",
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5..."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    additionalProperties = AdditionalPropertiesValue.TRUE,
                                    example = """
                                            {
                                              "status": "error",
                                              "message": "Invalid credentials"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping(value = "/login-api", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> loginCustom(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            String token = jwtService.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "user", userDetails.getUsername(),
                    "roles", userDetails.getAuthorities().toString(),
                    "token", token
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Invalid credentials"));
        }
    }
}
