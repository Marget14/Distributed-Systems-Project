package com.streetfoodgo.web.rest;

import com.streetfoodgo.core.service.TicketDataService;
import com.streetfoodgo.core.service.model.TicketView;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing {@code Ticket} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/ticket", produces = MediaType.APPLICATION_JSON_VALUE)
public class TicketResource {

    private final TicketDataService ticketDataService;

    public TicketResource(final TicketDataService ticketDataService) {
        if (ticketDataService == null) throw new NullPointerException();
        this.ticketDataService = ticketDataService;
    }

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<TicketView> tickets() {
        final List<TicketView> ticketViewList = this.ticketDataService.getAllTickets();
        return ticketViewList;
    }
}
