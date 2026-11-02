package com.furniture.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.furniture.entity.TaskPriority;
import com.furniture.entity.TaskStatus;

public class WorkTaskResponse {

    private Long id;
    private String taskName;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private String projectName;

    public WorkTaskResponse() {
    }

    public WorkTaskResponse(
            Long id,
            String taskName,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate,
            LocalDateTime completedAt,
            String projectName) {

        this.id = id;
        this.taskName = taskName;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.projectName = projectName;
    }

    public Long getId() { return id; }
    public String getTaskName() { return taskName; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getProjectName() { return projectName; }
}