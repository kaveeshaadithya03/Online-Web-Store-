package com.example.web_store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @Column(name = "ticket_id")
    private String ticketID;

    @NotBlank(message = "Customer message cannot be empty")
    @Column(name = "customer_message")
    private String customerMessage;

    @Column(name = "service_reply")
    private String serviceReply;

    @Column(name = "user_id")
    private String userId;

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public void setCustomerMessage(String customerMessage) {
        this.customerMessage = customerMessage;
    }

    public String getServiceReply() {
        return serviceReply;
    }

    public void setServiceReply(String serviceReply) {
        this.serviceReply = serviceReply;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}