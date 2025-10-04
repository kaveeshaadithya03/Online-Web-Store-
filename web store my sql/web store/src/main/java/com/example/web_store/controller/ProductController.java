package com.example.web_store.controller;

import com.example.web_store.model.Product;
import com.example.web_store.model.User;
import com.example.web_store.repository.ProductRepository;
import com.example.web_store.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to get authenticated seller's ID
    private String getAuthenticatedSellerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            logger.debug("Fetching user for email: {}", email);
            User user = userRepository.findByEmail(email);
            if (user != null && "seller".equals(user.getRole())) {
                return user.getId();
            }
            logger.error("User with email {} is not a seller or not found", email);
            throw new SecurityException("Unauthorized access: No authenticated seller found");
        }
        logger.error("No authenticated user found");
        throw new SecurityException("Unauthorized access: No authenticated user");
    }

    @GetMapping("/public")
    public ResponseEntity<List<Product>> getAllPublicProducts() {
        logger.info("Fetching all public products");
        List<Product> products = productRepository.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Product> getPublicProductById(@PathVariable String id) {
        logger.info("Fetching public product with ID: {}", id);
        Optional<Product> productOptional = productRepository.findById(id);
        return productOptional.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Product with ID {} not found", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Fetching products for seller");
        try {
            String sellerId = getAuthenticatedSellerId();
            List<Product> products = productRepository.findBySellerId(sellerId);
            logger.info("Found {} products for seller ID: {}", products.size(), sellerId);
            return ResponseEntity.ok(products);
        } catch (SecurityException e) {
            logger.error("Security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        logger.info("Fetching product with ID: {} for seller", id);
        try {
            String sellerId = getAuthenticatedSellerId();
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                if (!product.getSellerId().equals(sellerId)) {
                    logger.warn("Product ID {} does not belong to seller ID {}", id, sellerId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return ResponseEntity.ok(product);
            }
            logger.warn("Product with ID {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            logger.error("Security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<?> addProduct(@Valid @RequestBody Product product) {
        logger.info("Adding new product with productID: {}", product.getProductID());
        try {
            String sellerId = getAuthenticatedSellerId();
            if (productRepository.existsByProductID(product.getProductID())) {
                logger.warn("Product ID {} already exists", product.getProductID());
                return ResponseEntity.badRequest().body("Product ID already exists");
            }
            product.setId(UUID.randomUUID().toString());
            product.setSellerId(sellerId);
            Product savedProduct = productRepository.save(product);
            logger.info("Product {} saved successfully", product.getProductID());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (SecurityException e) {
            logger.error("Security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody Product updatedProduct) {
        logger.info("Updating product with ID: {}", id);
        try {
            String sellerId = getAuthenticatedSellerId();
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isEmpty()) {
                logger.warn("Product with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }
            Product existingProduct = productOptional.get();
            if (!existingProduct.getSellerId().equals(sellerId)) {
                logger.warn("Product ID {} does not belong to seller ID {}", id, sellerId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Product does not belong to this seller");
            }
            if (!existingProduct.getProductID().equals(updatedProduct.getProductID()) &&
                    productRepository.existsByProductID(updatedProduct.getProductID())) {
                logger.warn("Product ID {} already exists", updatedProduct.getProductID());
                return ResponseEntity.badRequest().body("Product ID already exists");
            }
            existingProduct.setProductID(updatedProduct.getProductID());
            existingProduct.setProductName(updatedProduct.getProductName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setProductPrice(updatedProduct.getProductPrice());
            existingProduct.setProductCount(updatedProduct.getProductCount());
            existingProduct.setImages(updatedProduct.getImages());
            existingProduct.setSellerId(sellerId);
            Product savedProduct = productRepository.save(existingProduct);
            logger.info("Product {} updated successfully", id);
            return ResponseEntity.ok(savedProduct);
        } catch (SecurityException e) {
            logger.error("Security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_seller')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        logger.info("Deleting product with ID: {}", id);
        try {
            String sellerId = getAuthenticatedSellerId();
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                if (!product.getSellerId().equals(sellerId)) {
                    logger.warn("Product ID {} does not belong to seller ID {}", id, sellerId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                productRepository.deleteById(id);
                logger.info("Product {} deleted successfully", id);
                return ResponseEntity.ok().build();
            }
            logger.warn("Product with ID {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            logger.error("Security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Custom exception handler for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}