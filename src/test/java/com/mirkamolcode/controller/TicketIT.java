package com.mirkamolcode.controller;

import com.mirkamolcode.AbstractTestConfig;
import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.dto.TicketResponse;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.entity.enums.Priority;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.CommentRepository;
import com.mirkamolcode.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TicketIT extends AbstractTestConfig {
    private static final String TICKET_BASE_URL = "/leads";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Test
    void canGetAllTickets() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Payment issue");
        ticket.setCategory(Category.BILLING);
        ticket.setPriority(Priority.HIGH);
        ticket.setSummary("Unable to complete payment");

        ticketRepository.save(ticket);

        webTestClient.get()
                .uri(TICKET_BASE_URL)
                .headers(h -> h.setBasicAuth("admin123", "password"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ApiResponse<List<TicketResponse>>>() {
                })
                .value(response -> {
                    assertThat(response).isNotNull();
                    List<TicketResponse> t = response.getBody();
                    assertThat(t).isNotEmpty();
                    assertThat(t.getFirst().title()).isEqualTo(ticket.getTitle());
                    assertThat(t.getFirst().category()).isEqualTo(ticket.getCategory());
                });
    }

    @Test
    void canGetTicketById() {
        ApplicationUser user = userRepository.findApplicationUserByUsername("user123")
                .orElseThrow();

        Comment comment = new Comment(
                "test@gmail.com",
                "I am having issue with payment"
        );
        comment.setUser(user);
        comment = commentRepository.save(comment);

        Ticket savedTicket = ticketRepository.save(
                new Ticket("Payment issue",
                        Category.BILLING,
                        Priority.HIGH,
                        "Unable to complete payment",
                        comment
                )
        );

        ApiResponse<TicketResponse> apiResponse = webTestClient.get()
                .uri(TICKET_BASE_URL + "/{id}", savedTicket.getId())
                .headers(h -> h.setBasicAuth("admin123", "password"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<ApiResponse<TicketResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(apiResponse).isNotNull();
        TicketResponse ticketResponse = apiResponse.getBody();
        assertThat(ticketResponse).isNotNull();
        assertThat(ticketResponse.id()).isEqualTo(savedTicket.getId());
        assertThat(ticketResponse.title()).isEqualTo(savedTicket.getTitle());
        assertThat(ticketResponse.category()).isEqualTo(savedTicket.getCategory());
        assertThat(ticketResponse.priority()).isEqualTo(savedTicket.getPriority());
        assertThat(ticketResponse.summary()).isEqualTo(savedTicket.getSummary());
    }

    @Test
    void shouldReturn404WhenTicketDoesNotExist() {
        webTestClient.get()
                .uri(TICKET_BASE_URL + "/{id}", UUID.randomUUID())
                .headers(h -> h.setBasicAuth("admin123", "password"))
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
