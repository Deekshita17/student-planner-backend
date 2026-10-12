package com.studentplanner.controller;

import com.studentplanner.dto.DashboardResponse;
import com.studentplanner.security.UserDetailsImpl;
import com.studentplanner.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing dashboard metrics:
 * today's tasks, overdue tasks, and task completion percentage.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard: Returns real-time metrics for the logged-in student.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardSummary(
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        DashboardResponse summary = dashboardService.getDashboardSummary(userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }
}
