package com.studentplanner;

import com.studentplanner.dto.AuthResponse;
import com.studentplanner.dto.RegisterRequest;
import com.studentplanner.entity.User;
import com.studentplanner.exception.BadRequestException;
import com.studentplanner.repository.UserRepository;
import com.studentplanner.security.JwtUtils;
import com.studentplanner.security.UserDetailsImpl;
import com.studentplanner.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and unit tests validating core application services,
 * database connectivity, and JWT security operations.
 */
@SpringBootTest
class StudentPlannerApplicationTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up test user to ensure idempotent test runs
        userRepository.findByEmail("test_student@example.com").ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("Context loads and PasswordEncoder hashes passwords correctly")
    void testPasswordEncoder() {
        String rawPassword = "securePassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    @DisplayName("JwtUtils creates and validates JWT tokens with expected claims")
    void testJwtTokenGenerationAndValidation() {
        User testUser = User.builder()
                .id(999L)
                .name("Alex Student")
                .email("alex@example.com")
                .password("hash")
                .build();

        UserDetailsImpl userPrincipal = UserDetailsImpl.build(testUser);
        String token = jwtUtils.generateJwtToken(userPrincipal);

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("alex@example.com", jwtUtils.getUserNameFromJwtToken(token));
        assertEquals(999L, jwtUtils.getUserIdFromJwtToken(token));
    }

    @Test
    @DisplayName("AuthService successfully registers a student and rejects duplicate emails")
    void testRegisterAndDuplicateValidation() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test Student")
                .email("test_student@example.com")
                .password("password123")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test_student@example.com", response.getEmail());
        assertEquals("Test Student", response.getName());

        // Attempting to register the same email must throw BadRequestException
        assertThrows(BadRequestException.class, () -> authService.register(request));
    }
}
