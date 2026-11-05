package com.furniture.dto;

import java.time.LocalDate;

import com.furniture.entity.ProjectStatus;

/**
 * Safe project representation for endpoints available to employees.
 * Financial and client-contact fields are intentionally excluded.
 */
public record ProjectSummaryResponse(
        Long id,
        String projectName,
        String description,
        LocalDate startDate,
        LocalDate expectedEndDate,
        ProjectStatus projectStatus) {
}
