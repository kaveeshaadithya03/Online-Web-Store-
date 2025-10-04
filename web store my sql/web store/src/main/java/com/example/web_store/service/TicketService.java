package com.example.web_store.service;

import com.example.web_store.model.Ticket;
import com.example.web_store.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByUserId(String userId) {
        return ticketRepository.findByUserId(userId);
    }

    public Ticket getTicketById(String ticketID) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketID);
        return ticketOptional.orElse(null);
    }

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(String ticketID, String serviceReply) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketID);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            ticket.setServiceReply(serviceReply);
            return ticketRepository.save(ticket);
        }
        return null;
    }

    public void deleteTicket(String ticketID) {
        ticketRepository.deleteById(ticketID);
    }
}