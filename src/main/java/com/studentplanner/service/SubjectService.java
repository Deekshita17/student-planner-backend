package com.studentplanner.service;

import com.studentplanner.dto.SubjectRequest;
import com.studentplanner.dto.SubjectResponse;
import com.studentplanner.entity.Subject;
import com.studentplanner.entity.User;
import com.studentplanner.exception.BadRequestException;
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
 * SubjectService manages academic courses (subjects).
 * Ensures that every subject operation is strictly isolated to the requesting user.
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Retrieves all subjects belonging to the authenticated student.
     */
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects(Long userId) {
        return subjectRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single subject by ID, verifying user ownership.
     */
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id, Long userId) {
        Subject subject = findUserSubjectOrThrow(id, userId);
        return mapToResponse(subject);
    }

    /**
     * Creates a new subject for the authenticated student.
     */
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request, Long userId) {
        String trimmedName = request.getName().trim();

        if (subjectRepository.existsByNameIgnoreCaseAndUserId(trimmedName, userId)) {
            throw new BadRequestException("You already have a subject named '" + trimmedName + "'");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Subject subject = Subject.builder()
                .name(trimmedName)
                .color(request.getColor().trim().toUpperCase())
                .user(user)
                .build();

        Subject saved = subjectRepository.save(subject);
        return mapToResponse(saved);
    }

    /**
     * Updates an existing subject's name and color.
     */
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request, Long userId) {
        Subject subject = findUserSubjectOrThrow(id, userId);
        String trimmedName = request.getName().trim();

        if (subjectRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(trimmedName, userId, id)) {
            throw new BadRequestException("You already have another subject named '" + trimmedName + "'");
        }

        subject.setName(trimmedName);
        subject.setColor(request.getColor().trim().toUpperCase());

        Subject updated = subjectRepository.save(subject);
        return mapToResponse(updated);
    }

    /**
     * Deletes a subject and its associated tasks.
     */
    @Transactional
    public void deleteSubject(Long id, Long userId) {
        Subject subject = findUserSubjectOrThrow(id, userId);
        subjectRepository.delete(subject);
    }

    /**
     * Helper method to verify the subject exists and belongs to this user.
     */
    private Subject findUserSubjectOrThrow(Long id, Long userId) {
        return subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject with id " + id + " not found or does not belong to you"));
    }

    /**
     * Maps Subject entity to SubjectResponse DTO with task count.
     */
    private SubjectResponse mapToResponse(Subject subject) {
        long count = taskRepository.countBySubjectId(subject.getId());
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .color(subject.getColor())
                .taskCount(count)
                .build();
    }
}
