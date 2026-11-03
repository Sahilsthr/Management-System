package com.furniture.dto;

import java.time.LocalDate;

public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String designation;
    private LocalDate joiningDate;
    private String employeeName;
    private String email;

    public EmployeeResponse(
            Long id,
            String employeeCode,
            String designation,
            LocalDate joiningDate,
            String employeeName,
            String email) {

        this.id = id;
        this.employeeCode = employeeCode;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.employeeName = employeeName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getDesignation() {
        return designation;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmail() {
        return email;
    }
}
