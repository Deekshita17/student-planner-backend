package com.studentplanner.service;

import com.studentplanner.dto.AuthResponse;
import com.studentplanner.dto.LoginRequest;
import com.studentplanner.dto.RegisterRequest;
import com.studentplanner.entity.User;
import com.studentplanner.exception.BadRequestException;
import com.studentplanner.repository.UserRepository;
import com.studentplanner.security.JwtUtils;
import com.studentplanner.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService handles user registration and authentication workflows.
 * Encapsulates password hashing and token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Registers a new student account.
     * Checks if email is already taken, hashes password, saves to DB,
     * and generates a JWT token so the student is immediately logged in.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with this email already exists: " + normalizedEmail);
        }

        // Build new User entity with securely hashed password
        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // Build UserDetails and generate initial JWT token
        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String jwt = jwtUtils.generateJwtToken(userDetails);

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Authenticates existing student credentials and returns a JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // AuthenticationManager checks email and BCrypt password match
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userPrincipal);

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userPrincipal.getId())
                .name(userPrincipal.getName())
                .email(userPrincipal.getUsername())
                .build();
    }
}
