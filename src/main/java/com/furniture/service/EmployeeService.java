
package com.furniture.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateEmployeeRequest;
import com.furniture.dto.EmployeeResponse;
import com.furniture.entity.Employee;
import com.furniture.entity.Role;
import com.furniture.entity.User;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id " + request.getUserId()));

        if (user.getRole() != Role.EMPLOYEE) {
            throw new IllegalArgumentException(
                    "Employee profile can only be assigned to EMPLOYEE users");
        }

        if (employeeRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException(
                    "Employee already exists for this user");
        }

        String employeeCode = request.getEmployeeCode().trim();

        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            throw new DuplicateResourceException(
                    "Employee code already exists");
        }

        Employee employee = new Employee();

        employee.setEmployeeCode(employeeCode);
        employee.setDesignation(request.getDesignation().trim());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setUser(user);

        Employee savedEmployee = employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployee() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id " + id));

        return mapToResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        Employee employee = employeeRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee profile not found"));

        return mapToResponse(employee);
    }

    private EmployeeResponse mapToResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getDesignation(),
                employee.getJoiningDate(),
                employee.getUser().getName(),
                employee.getUser().getEmail()
        );
    }
}
