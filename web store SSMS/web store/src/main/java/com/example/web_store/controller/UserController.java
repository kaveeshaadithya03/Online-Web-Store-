package com.example.web_store.controller;

import com.example.web_store.model.Product;
import com.example.web_store.model.User;
import com.example.web_store.repository.ProductRepository;
import com.example.web_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (userRepository.existsByEmail(updatedUser.getEmail()) && !updatedUser.getEmail().equals(existingUser.get().getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        updatedUser.setId(id);
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        } else {
            updatedUser.setPassword(existingUser.get().getPassword());
        }
        User savedUser = userRepository.save(updatedUser);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<?> addProduct(@Valid @RequestBody Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null || !"seller".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Invalid seller email or role");
        }
        if (productRepository.existsByProductID(product.getProductID())) {
            return ResponseEntity.badRequest().body("Product ID already exists");
        }
        product.setId(UUID.randomUUID().toString());
        product.setSellerId(user.getId()); // Set sellerId before saving
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<?> getProduct(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null || !"seller".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Invalid seller email or role");
        }
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!user.getId().equals(existingProduct.get().getSellerId())) {
            return ResponseEntity.status(403).body("Unauthorized: Product does not belong to this seller");
        }
        return ResponseEntity.ok(existingProduct.get());
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody Product updatedProduct) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null || !"seller".equals(user.getRole())) {
            return ResponseEntity.status(403).body("Invalid seller email or role");
        }
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!user.getId().equals(existingProduct.get().getSellerId())) {
            return ResponseEntity.status(403).body("Unauthorized: Product does not belong to this seller");
        }
        updatedProduct.setId(id);
        updatedProduct.setSellerId(user.getId()); // Ensure sellerId is set
        Product savedProduct = productRepository.save(updatedProduct);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public List<Product> getSellerProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        return productRepository.findBySellerId(user.getId());
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        if (user == null || !"seller".equals(user.getRole())) {
            return ResponseEntity.status(403).build();
        }
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!user.getId().equals(product.get().getSellerId())) {
            return ResponseEntity.status(403).build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}