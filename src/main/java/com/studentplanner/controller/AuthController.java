package com.studentplanner.controller;

import com.studentplanner.dto.AuthResponse;
import com.studentplanner.dto.LoginRequest;
import com.studentplanner.dto.RegisterRequest;
import com.studentplanner.security.UserDetailsImpl;
import com.studentplanner.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @RestController: Combines @Controller and @ResponseBody.
 * Tells Spring this class receives HTTP requests and serializes return values
 * directly into JSON format for the client.
 *
 * @RequestMapping("/api/auth"): Sets the base path for all authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * @PostMapping("/register"): Maps HTTP POST requests to /api/auth/register.
     * @Valid: Ensures the RegisterRequest object passes all validation rules (@NotBlank, @Email)
     * before the method body runs.
     * @RequestBody: Converts the incoming JSON body into a Java RegisterRequest object.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Handles student login requests.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * @AuthenticationPrincipal: Injects the currently authenticated user's details.
     * Allows the frontend to verify token validity and get fresh profile details.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", userPrincipal.getId());
        profile.put("name", userPrincipal.getName());
        profile.put("email", userPrincipal.getUsername());
        return ResponseEntity.ok(profile);
    }
}
