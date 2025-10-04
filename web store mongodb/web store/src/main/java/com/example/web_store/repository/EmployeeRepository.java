package com.example.web_store.repository;

import com.example.web_store.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    boolean existsByEmail(String email);
    Employee findByEmail(String email);
    List<Employee> findByRolesContaining(String role);  // Native support for collection contains
}