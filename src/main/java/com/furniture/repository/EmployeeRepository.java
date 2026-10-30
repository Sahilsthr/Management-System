package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.furniture.entity.Employee;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByUserId(Long userId);
    Optional<Employee> findByUser_Id(Long userId);
}