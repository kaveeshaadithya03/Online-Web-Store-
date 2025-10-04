package com.example.web_store.repository;

import com.example.web_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.role = ?1")
    List<User> findByRole(String role);
}