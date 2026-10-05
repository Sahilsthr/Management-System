package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.Employee;
import com.furniture.service.EmployeeService;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/api/employees")
    public Employee createEmployee(@RequestBody Employee employee) {
        System.out.println("RECEIVED designation=" + employee.getDesignation());
        return employeeService.createEmployee(employee);
    }

    @GetMapping("/api/employees")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployee();
    }

    @GetMapping("/api/employees/{id}")
    public Optional<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }
}