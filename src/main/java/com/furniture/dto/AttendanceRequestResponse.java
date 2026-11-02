package com.furniture.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.furniture.entity.AttendanceRequestStatus;

public class AttendanceRequestResponse {

    private Long id;
    private String employeeName;
    private String employeeCode;
    private LocalDate attendanceDate;
    private AttendanceRequestStatus status;
    private String projectName;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private String remark;

    public AttendanceRequestResponse(
            Long id,
            String employeeName,
            String employeeCode,
            LocalDate attendanceDate,
            AttendanceRequestStatus status,
            String projectName,
            LocalDateTime requestedAt,
            LocalDateTime reviewedAt,
            String remark) {

        this.id = id;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.projectName = projectName;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
        this.remark = remark;
    }

    public Long getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeCode() { return employeeCode; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public AttendanceRequestStatus getStatus() { return status; }
    public String getProjectName() { return projectName; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getRemark() { return remark; }
}
