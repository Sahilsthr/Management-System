package com.furniture.dto;

import jakarta.validation.constraints.NotNull;

public class AssignEmployeeRequest {

	@NotNull
	private Long projectId;

	@NotNull
	private Long employeeId;

	public AssignEmployeeRequest() {
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}
}
