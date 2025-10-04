package com.example.web_store.repository;

import com.example.web_store.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    boolean existsByEmail(String email);
    Employee findByEmail(String email);
    @Query(value = "SELECT * FROM employees e WHERE LOWER(e.role) LIKE LOWER(CONCAT('%', :role, '%'))", nativeQuery = true)
    List<Employee> findByRolesContaining(String role);
}