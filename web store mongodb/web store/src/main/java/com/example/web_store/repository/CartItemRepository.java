package com.example.web_store.repository;

import com.example.web_store.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    Optional<CartItem> findByIdAndUserId(String id, String userId);
}