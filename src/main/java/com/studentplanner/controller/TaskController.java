package com.studentplanner.controller;

import com.studentplanner.dto.TaskRequest;
import com.studentplanner.dto.TaskResponse;
import com.studentplanner.entity.Priority;
import com.studentplanner.security.UserDetailsImpl;
import com.studentplanner.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller handling task management:
 * add, edit, delete, mark done, and filter by subject, priority, and status.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * GET /api/tasks: Returns tasks for the student, with optional query filters:
     * - subjectId: filter by specific subject ID
     * - priority: LOW, MEDIUM, or HIGH
     * - completed: true or false
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean completed,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {

        List<TaskResponse> tasks = taskService.getFilteredTasks(
                userPrincipal.getId(),
                subjectId,
                priority,
                completed
        );
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/tasks/{id}: Returns a single task by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        TaskResponse response = taskService.getTaskById(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/tasks: Creates a new study task.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        TaskResponse response = taskService.createTask(request, userPrincipal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/tasks/{id}: Updates an existing task's title, subject, due date, or priority.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        TaskResponse response = taskService.updateTask(id, request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/tasks/{id}/toggle: Toggles task completion between done and pending.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TaskResponse> toggleTaskComplete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        TaskResponse response = taskService.toggleTaskComplete(id, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/tasks/{id}: Permanently deletes a task.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        taskService.deleteTask(id, userPrincipal.getId());
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}
