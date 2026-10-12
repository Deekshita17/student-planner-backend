package com.studentplanner.service;

import com.studentplanner.dto.DashboardResponse;
import com.studentplanner.dto.TaskResponse;
import com.studentplanner.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardService aggregates high-level progress statistics and quick-access
 * lists for the student dashboard (today's tasks, overdue tasks, completion bar).
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    /**
     * Computes completion metrics and fetches urgent tasks for the logged-in student.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary(Long userId) {
        LocalDate today = LocalDate.now();

        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndCompletedTrue(userId);
        long pendingTasks = Math.max(0, totalTasks - completedTasks);

        double completionPercentage = 0.0;
        if (totalTasks > 0) {
            // Round to 1 decimal place, e.g. 75.5%
            completionPercentage = Math.round(((double) completedTasks / totalTasks) * 1000.0) / 10.0;
        }

        List<TaskResponse> todayTasks = taskRepository
                .findByUserIdAndDueDateOrderByPriorityDesc(userId, today)
                .stream()
                .map(taskService::mapToResponse)
                .collect(Collectors.toList());

        List<TaskResponse> overdueTasks = taskRepository
                .findByUserIdAndDueDateLessThanAndCompletedFalseOrderByDueDateAsc(userId, today)
                .stream()
                .map(taskService::mapToResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .completionPercentage(completionPercentage)
                .todayTasks(todayTasks)
                .overdueTasks(overdueTasks)
                .build();
    }
}
