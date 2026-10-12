package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.User;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    public Employee createEmployee(Employee employee) {
    if (employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()) {
        throw new BadRequestException("employeeCode is required");
    }
    if (employee.getDesignation() == null || employee.getDesignation().isBlank()) {
        throw new BadRequestException("designation is required");
    }
    if (employee.getJoiningDate() == null) {
        throw new BadRequestException("joiningDate is required");
    }
    if (employee.getUser() == null || employee.getUser().getId() == null) {
        throw new BadRequestException("user.id is required");
    }

    Long userId = employee.getUser().getId();
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

    if (employeeRepository.existsByUserId(userId)) {
        throw new DuplicateResourceException("Employee already exists for this user");
    }
    if (employeeRepository.existsByEmployeeCode(employee.getEmployeeCode())) {
        throw new DuplicateResourceException("Employee code already exists");
    }

    employee.setUser(user);
    return employeeRepository.save(employee);
}

    public List<Employee> getAllEmployee() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found with " + id));
    }
}