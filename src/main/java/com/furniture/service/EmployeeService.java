package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.dto.CreateEmployeeRequest;
import com.furniture.entity.Employee;
import com.furniture.entity.User;
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

    public Employee createEmployee(CreateEmployeeRequest request) {
    User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with id " + request.getUserId()));

    if (employeeRepository.existsByUserId(request.getUserId())) {
        throw new DuplicateResourceException("Employee already exists for this user");
    }
    if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
        throw new DuplicateResourceException("Employee code already exists");
    }

    Employee employee = new Employee();
    employee.setEmployeeCode(request.getEmployeeCode());
    employee.setDesignation(request.getDesignation());
    employee.setJoiningDate(request.getJoiningDate());
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