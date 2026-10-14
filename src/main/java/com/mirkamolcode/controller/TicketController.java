package com.mirkamolcode.controller;

import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.dto.TicketResponse;
import com.mirkamolcode.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leads")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ticketList:read')")
    public ApiResponse<List<TicketResponse>> getAll() {
        return ApiResponse.success("Tickets retrieved successfully", ticketService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tickets:read')")
    public ApiResponse<TicketResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("Ticket retrieved successfully", ticketService.getById(id));
    }
}
