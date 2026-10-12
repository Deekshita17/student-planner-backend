package com.studentplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject represents an academic course or category (e.g. "Mathematics", "Physics").
 * Each subject has a name, a display color (e.g., hex code #3B82F6), and belongs to one user.
 */
@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Color hex code (e.g. #4F46E5) used in frontend badges and UI cards.
     */
    @Column(nullable = false, length = 20)
    private String color;

    /**
     * @ManyToOne: Many subjects belong to one user.
     * @JoinColumn: Creates the foreign key column 'user_id' in the 'subjects' table.
     * fetch = FetchType.LAZY: Don't load the full user object until explicitly accessed, saving memory.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * @OneToMany: One subject can contain many study tasks.
     * If a subject is deleted, its associated tasks are also removed.
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
