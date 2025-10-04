package com.example.web_store.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;

@Document(collection = "tickets")
public class Ticket {
    @Id
    private String ticketID;

    @NotBlank(message = "Customer message cannot be empty")
    private String customerMessage;

    private String serviceReply;

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