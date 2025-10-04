package com.example.web_store.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Date;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @NotBlank(message = "Order ID is required")
    @Column(name = "order_id")
    private String orderID;

    @Positive(message = "Price must be positive")
    @Column(name = "price")
    private double price;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate = new Date();

    @Column(name = "status")
    private String status = "Pending";

    public Order() {}

    public Order(String orderID, double price) {
        this.orderID = orderID;
        this.price = price;
    }

    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}