package com.studentplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task represents an individual study assignment, reading, or deadline.
 * It tracks title, due date, priority level, completion status, and the associated subject.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /**
     * The target date by which the task should be finished.
     */
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * @Enumerated(EnumType.STRING): Stores the enum as readable text ("LOW", "MEDIUM", "HIGH")
     * in the database rather than an integer index (0, 1, 2).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    /**
     * Flag indicating whether the student has completed this task.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    /**
     * @ManyToOne: The subject (course) this task belongs to.
     * Can be null if the student creates a general task not tied to a specific course.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    /**
     * The owner student user. Used to enforce data isolation (each user sees only their own data).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
