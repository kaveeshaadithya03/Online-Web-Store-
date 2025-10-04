package com.example.web_store.repository;

import com.example.web_store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsByProductID(String productID);
    List<Product> findBySellerId(String sellerId);
}