package com.example.EmployeeService.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.EmployeeService.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
