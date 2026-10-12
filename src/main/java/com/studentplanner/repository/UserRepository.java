package com.studentplanner.repository;

import com.studentplanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Repository: Tells Spring that this interface is a Data Access Object (DAO).
 * By extending JpaRepository<User, Long>, Spring Data JPA automatically generates
 * CRUD methods (save, findById, findAll, delete, etc.) without writing any SQL!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     * Spring Data JPA interprets this method name as:
     * "SELECT * FROM users WHERE email = ?1"
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if any user already exists with this email address.
     * Generates: "SELECT COUNT(*) > 0 FROM users WHERE email = ?1"
     */
    boolean existsByEmail(String email);
}
