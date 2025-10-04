package com.example.web_store.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @Column(name = "id")
    private String id;

    @NotBlank(message = "Name is required")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Convert(converter = StringSetConverter.class)
    private Set<String> roles = new HashSet<>();

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CUSTOMER_SERVICE = "CUSTOMER_SERVICE";
    public static final String ROLE_FINANCE_EXECUTION = "FINANCE_EXECUTION";
    public static final String ROLE_MARKETING_EXECUTIVE = "MARKETING_EXECUTIVE";

    public Employee() {}

    public Employee(String name, String email, String password, Set<String> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        setRoles(roles);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) {
        this.roles.clear();
        if (roles != null) {
            for (String role : roles) {
                if (isValidRole(role)) this.roles.add(role);
            }
        }
    }

    private boolean isValidRole(String role) {
        return role != null && (
                role.equals(ROLE_ADMIN) ||
                        role.equals(ROLE_CUSTOMER_SERVICE) ||
                        role.equals(ROLE_FINANCE_EXECUTION) ||
                        role.equals(ROLE_MARKETING_EXECUTIVE)
        );
    }

    @Converter
    public static class StringSetConverter implements AttributeConverter<Set<String>, String> {
        @Override
        public String convertToDatabaseColumn(Set<String> attribute) {
            return attribute == null || attribute.isEmpty() ? null : String.join(",", attribute);
        }

        @Override
        public Set<String> convertToEntityAttribute(String dbData) {
            return dbData == null || dbData.isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(dbData.split(",")));
        }
    }
}