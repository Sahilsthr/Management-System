package com.furniture.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.User;
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
        if (employee.getDesignation() == null || employee.getDesignation().isBlank()) {
            throw new IllegalArgumentException("Designation is required");
        }
        if (employee.getUser() == null || employee.getUser().getId() == null) {
            throw new IllegalArgumentException("user.id is required");
        }
        User user = userRepository.findById(employee.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployee() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
}