package com.studentplanner.service;

import com.studentplanner.dto.TaskRequest;
import com.studentplanner.dto.TaskResponse;
import com.studentplanner.entity.Priority;
import com.studentplanner.entity.Subject;
import com.studentplanner.entity.Task;
import com.studentplanner.entity.User;
import com.studentplanner.exception.ResourceNotFoundException;
import com.studentplanner.repository.SubjectRepository;
import com.studentplanner.repository.TaskRepository;
import com.studentplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskService manages student tasks and deadlines.
 * Enforces user data isolation so each user only sees and modifies their own tasks.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves tasks for the user, with optional filters for subject, priority, and completion status.
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getFilteredTasks(Long userId, Long subjectId, Priority priority, Boolean completed) {
        return taskRepository.findFilteredTasks(userId, subjectId, priority, completed).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single task by ID for the authenticated user.
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id, Long userId) {
        Task task = findUserTaskOrThrow(id, userId);
        return mapToResponse(task);
    }

    /**
     * Creates a new task assigned to the authenticated user.
     */
    @Transactional
    public TaskResponse createTask(TaskRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndUserId(request.getSubjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subject not found or does not belong to you with id: " + request.getSubjectId()));
        }

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .completed(Boolean.TRUE.equals(request.getCompleted()))
                .subject(subject)
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing task's title, subject, due date, priority, or completed status.
     */
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, Long userId) {
        Task task = findUserTaskOrThrow(id, userId);

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndUserId(request.getSubjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subject not found or does not belong to you with id: " + request.getSubjectId()));
        }

        task.setTitle(request.getTitle().trim());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }
        task.setSubject(subject);

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    /**
     * Toggles the completion status of a task (mark done / undo).
     */
    @Transactional
    public TaskResponse toggleTaskComplete(Long id, Long userId) {
        Task task = findUserTaskOrThrow(id, userId);
        task.setCompleted(!task.isCompleted());
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    /**
     * Deletes a task by ID.
     */
    @Transactional
    public void deleteTask(Long id, Long userId) {
        Task task = findUserTaskOrThrow(id, userId);
        taskRepository.delete(task);
    }

    /**
     * Helper to verify a task exists and belongs to the authenticated user.
     */
    private Task findUserTaskOrThrow(Long id, Long userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task with id " + id + " not found or does not belong to you"));
    }

    /**
     * Converts a Task entity to its TaskResponse DTO.
     */
    public TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .completed(task.isCompleted())
                .subjectId(task.getSubject() != null ? task.getSubject().getId() : null)
                .subjectName(task.getSubject() != null ? task.getSubject().getName() : null)
                .subjectColor(task.getSubject() != null ? task.getSubject().getColor() : null)
                .createdAt(task.getCreatedAt())
                .build();
    }
}
