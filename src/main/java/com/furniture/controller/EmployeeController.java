package com.furniture.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.CreateEmployeeRequest;
import com.furniture.dto.EmployeeResponse;
import com.furniture.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/api/employees")
    public EmployeeResponse createEmployee(
            @Valid @RequestBody CreateEmployeeRequest employee) {

        return employeeService.createEmployee(employee);
    }

    @GetMapping("/api/employees")
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployee();
    }

    @GetMapping("/api/employees/me")
    public EmployeeResponse getMyProfile(Authentication authentication) {
        return employeeService.getMyProfile(authentication.getName());
    }

    @GetMapping("/api/employees/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }
}