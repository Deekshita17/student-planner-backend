package com.studentplanner.dto;

import com.studentplanner.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO sent to the frontend when returning Task information.
 * Flattens the nested Subject entity so the React components can directly
 * display subject name and color badges without dealing with deep objects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private LocalDate dueDate;
    private Priority priority;
    private boolean completed;
    private Long subjectId;
    private String subjectName;
    private String subjectColor;
    private LocalDateTime createdAt;
}
