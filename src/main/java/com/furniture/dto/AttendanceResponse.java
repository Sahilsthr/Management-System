
package com.furniture.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.furniture.entity.AttendanceStatus;

public class AttendanceResponse {

    private Long id;
    private String employeeName;
    private String employeeCode;
    private String projectName;
    private LocalDate attendanceDate;
    private AttendanceStatus attendanceStatus;
    private LocalDateTime markedAt;

    public AttendanceResponse(
            Long id,
            String employeeName,
            String employeeCode,
            String projectName,
            LocalDate attendanceDate,
            AttendanceStatus attendanceStatus,
            LocalDateTime markedAt) {

        this.id = id;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.projectName = projectName;
        this.attendanceDate = attendanceDate;
        this.attendanceStatus = attendanceStatus;
        this.markedAt = markedAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }
}
