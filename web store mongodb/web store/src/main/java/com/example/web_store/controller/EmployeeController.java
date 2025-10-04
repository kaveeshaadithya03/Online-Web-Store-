package com.example.web_store.controller;

import com.example.web_store.model.Employee;
import com.example.web_store.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/admins")
    public List<Employee> getAdmins() {
        return employeeRepository.findByRolesContaining(Employee.ROLE_ADMIN);
    }

    @GetMapping("/non-admins")
    public List<Employee> getNonAdmins() {
        return employeeRepository.findAll().stream()
                .filter(employee -> employee.getRoles().stream().anyMatch(role -> !role.equals(Employee.ROLE_ADMIN)))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee) {
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        employee.setId(UUID.randomUUID().toString());
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        Employee savedEmployee = employeeRepository.save(employee);
        return ResponseEntity.ok(savedEmployee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @Valid @RequestBody Employee updatedEmployee) {
        Optional<Employee> existingEmployee = employeeRepository.findById(id);
        if (existingEmployee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (employeeRepository.existsByEmail(updatedEmployee.getEmail()) && !updatedEmployee.getEmail().equals(existingEmployee.get().getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        updatedEmployee.setId(id);
        if (updatedEmployee.getPassword() != null && !updatedEmployee.getPassword().isEmpty()) {
            updatedEmployee.setPassword(passwordEncoder.encode(updatedEmployee.getPassword()));
        } else {
            updatedEmployee.setPassword(existingEmployee.get().getPassword());
        }
        Employee savedEmployee = employeeRepository.save(updatedEmployee);
        return ResponseEntity.ok(savedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}