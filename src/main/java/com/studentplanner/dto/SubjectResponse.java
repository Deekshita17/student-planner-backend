package com.studentplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO sent to the frontend when listing or returning subjects.
 * Includes taskCount so the student can see how many tasks belong to this subject.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String name;
    private String color;
    private long taskCount;
}
