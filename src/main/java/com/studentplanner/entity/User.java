package com.studentplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Entity: Tells JPA/Hibernate that this Java class maps directly to a relational database table.
 * @Table: Specifies the name of the database table ("users") and constraints.
 * @Getter / @Setter: Lombok automatically generates getter and setter methods for all fields.
 * @NoArgsConstructor: Lombok generates a no-argument constructor required by Hibernate.
 * @AllArgsConstructor: Lombok generates a full-arguments constructor.
 * @Builder: Lombok provides a clean builder pattern (User.builder()...build()).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * @Id: Marks this field as the primary key of the table.
     * @GeneratedValue(strategy = GenerationType.IDENTITY): Delegates primary key generation
     * to the database's AUTO_INCREMENT feature.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's full name.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * User's email address, used as the login username.
     * Must be unique across all registered users.
     */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /**
     * Hashed password (never store plain text passwords!).
     */
    @Column(nullable = false)
    private String password;

    /**
     * Timestamp when the user registered.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @OneToMany: One user can have many subjects.
     * cascade = CascadeType.ALL: If a user is deleted, all their subjects are deleted too.
     * orphanRemoval = true: If a subject is removed from the user's list, delete it from the DB.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    /**
     * @OneToMany: One user can have many study tasks.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    /**
     * @PrePersist: Automatically runs before this entity is first inserted into the database.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
