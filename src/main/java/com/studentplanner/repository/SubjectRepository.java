package com.studentplanner.repository;

import com.studentplanner.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SubjectRepository provides database operations for Subject entities.
 * Includes methods ensuring a user can only access their own subjects.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Finds all subjects created by a specific user, sorted alphabetically by name.
     */
    List<Subject> findByUserIdOrderByNameAsc(Long userId);

    /**
     * Finds a specific subject by its ID, ensuring it belongs to the given user.
     * Prevents User A from viewing or modifying User B's subject!
     */
    Optional<Subject> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a user already has a subject with the same name (case-insensitive).
     */
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    /**
     * Checks if a user already has another subject with the same name during updates.
     */
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, Long userId, Long id);
}
