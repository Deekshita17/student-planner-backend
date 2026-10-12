package com.studentplanner.repository;

import com.studentplanner.entity.Priority;
import com.studentplanner.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TaskRepository handles database queries for student tasks.
 * All queries are scoped to the authenticated user's ID for data privacy.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds a task by ID only if it belongs to the given user.
     */
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds all tasks for a user, sorted by due date in ascending order.
     */
    List<Task> findByUserIdOrderByDueDateAsc(Long userId);

    /**
     * Flexible filtering query: filters by subject, priority, and completion status.
     * If any parameter is null, that condition is ignored.
     */
    @Query("SELECT t FROM Task t " +
           "LEFT JOIN t.subject s " +
           "WHERE t.user.id = :userId " +
           "AND (:subjectId IS NULL OR s.id = :subjectId) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "ORDER BY t.dueDate ASC, t.id DESC")
    List<Task> findFilteredTasks(@Param("userId") Long userId,
                                 @Param("subjectId") Long subjectId,
                                 @Param("priority") Priority priority,
                                 @Param("completed") Boolean completed);

    /**
     * Finds tasks due on a specific date (used for "Today's Tasks" on Dashboard).
     */
    List<Task> findByUserIdAndDueDateOrderByPriorityDesc(Long userId, LocalDate dueDate);

    /**
     * Finds incomplete tasks past their due date (used for "Overdue Tasks" on Dashboard).
     */
    List<Task> findByUserIdAndDueDateLessThanAndCompletedFalseOrderByDueDateAsc(Long userId, LocalDate today);

    /**
     * Counts total tasks owned by a user (for the progress bar calculation).
     */
    long countByUserId(Long userId);

    /**
     * Counts completed tasks owned by a user (for the progress bar calculation).
     */
    long countByUserIdAndCompletedTrue(Long userId);

    /**
     * Counts how many tasks belong to a subject.
     */
    long countBySubjectId(Long subjectId);
}
