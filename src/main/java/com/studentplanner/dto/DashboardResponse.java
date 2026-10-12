package com.studentplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated dashboard statistics and task lists for the student.
 * Feeds directly into the Dashboard page's progress bar, counters, and widgets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private double completionPercentage;
    private List<TaskResponse> todayTasks;
    private List<TaskResponse> overdueTasks;
}
