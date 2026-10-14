package com.furniture.dto;

import java.time.LocalDate;

import com.furniture.entity.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class CreateTaskRequest {

	@NotBlank
	private String taskName;
	private String description;
	private TaskPriority priority;
	private LocalDate dueDate;
	@NotNull
	private Long projectId;
	private Long assignedEmployeeId;

	public CreateTaskRequest() {
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getAssignedEmployeeId() {
		return assignedEmployeeId;
	}

	public void setAssignedEmployeeId(Long assignedEmployeeId) {
		this.assignedEmployeeId = assignedEmployeeId;
	}
}
