package com.studentplanner.controller;

import com.studentplanner.dto.SubjectRequest;
import com.studentplanner.dto.SubjectResponse;
import com.studentplanner.security.UserDetailsImpl;
import com.studentplanner.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller handling subject operations (add, edit, delete, list).
 * All endpoints require a valid JWT token and automatically resolve
 * the authenticated student's ID.
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * GET /api/subjects: Returns all subjects owned by the current student.
     */
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getAllSubjects(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<SubjectResponse> subjects = subjectService.getAllSubjects(userPrincipal.getId());
        return ResponseEntity.ok(subjects);
    }

    /**
     * GET /api/subjects/{id}: Returns a single subject by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        SubjectResponse response = subjectService.getSubjectById(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/subjects: Creates a new subject (e.g. "Biology", color "#10B981").
     */
    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @Valid @RequestBody SubjectRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        SubjectResponse response = subjectService.createSubject(request, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/subjects/{id}: Updates an existing subject's name or color.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        SubjectResponse response = subjectService.updateSubject(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/subjects/{id}: Deletes a subject and its associated tasks.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSubject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        subjectService.deleteSubject(id, userPrincipal.getId());
        return ResponseEntity.ok(Map.of("message", "Subject deleted successfully"));
    }
}
