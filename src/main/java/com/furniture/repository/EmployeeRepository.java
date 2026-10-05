package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}