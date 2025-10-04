package com.example.web_store.controller;

import com.example.web_store.model.Ticket;
import com.example.web_store.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER_SERVICE')")
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_customer')")
    public List<Ticket> getMyTickets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            throw new SecurityException("Unauthorized access");
        }
        return ticketService.getTicketsByUserId(email);
    }

    @GetMapping("/{ticketID}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER_SERVICE')")
    public ResponseEntity<Ticket> getTicket(@PathVariable String ticketID) {
        Ticket ticket = ticketService.getTicketById(ticketID);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('ROLE_customer')")
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null || email.equals("anonymousUser")) {
            throw new SecurityException("Unauthorized access");
        }
        ticket.setTicketID(UUID.randomUUID().toString());
        ticket.setUserId(email);
        ticket.setServiceReply(null); // Initial reply is null
        return ResponseEntity.ok(ticketService.createTicket(ticket));
    }

    public static class TicketUpdateDTO {
        @NotBlank(message = "Service reply cannot be empty")
        private String serviceReply;

        public String getServiceReply() {
            return serviceReply;
        }

        public void setServiceReply(String serviceReply) {
            this.serviceReply = serviceReply;
        }
    }

    @PutMapping("/{ticketID}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER_SERVICE')")
    public ResponseEntity<Ticket> updateTicket(@PathVariable String ticketID, @Valid @RequestBody TicketUpdateDTO ticketUpdateDTO) {
        Ticket updatedTicket = ticketService.updateTicket(ticketID, ticketUpdateDTO.getServiceReply());
        if (updatedTicket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{ticketID}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER_SERVICE')")
    public ResponseEntity<Void> deleteTicket(@PathVariable String ticketID) {
        ticketService.deleteTicket(ticketID);
        return ResponseEntity.noContent().build();
    }
}