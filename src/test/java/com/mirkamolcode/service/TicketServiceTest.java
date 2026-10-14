package com.mirkamolcode.service;

import com.mirkamolcode.dto.TicketResponse;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.entity.enums.Priority;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService underTest;

    @Test
    void shouldGetAllTickets() {
        // given
        Comment commentAboutBilling = new Comment("atbc@gmail.com", "I paid my subscription twice a month");
        Comment commentAboutAccount = new Comment("bdc@gmail.com", "I have difficulty with login");

        List<Ticket> expected = List.of(
                new Ticket("Billing issue", Category.BILLING, Priority.HIGH, "Billing occured twice", commentAboutBilling),
                new Ticket("Logging issue", Category.ACCOUNT, Priority.HIGH, "Can't log in", commentAboutAccount)
        );
        given(ticketRepository.findAll()).willReturn(expected);

        // when
        List<TicketResponse> actual = underTest.getAll();

        // then
        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.getFirst().title()).isEqualTo(expected.getFirst().getTitle());
    }

    @Test
    void shouldGetTicketById() {
        // given
        UUID id = UUID.randomUUID();
        Comment commentAboutAccount = new Comment("bdc@gmail.com", "I have difficulty with login");
        Ticket expected = new Ticket(id, "Logging issue", Category.ACCOUNT, Priority.HIGH, "Can't log in", commentAboutAccount, null);

        given(ticketRepository.findById(id)).willReturn(Optional.of(expected));

        // when
        TicketResponse actual = underTest.getById(id);

        // then
        assertThat(actual.title()).isEqualTo(expected.getTitle());
        assertThat(actual.summary()).isEqualTo(expected.getSummary());
    }

    @Test
    void shouldThrowWhenTicketNotFound() {
        // given
        UUID id = UUID.randomUUID();
        given(ticketRepository.findById(id)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.getById(id))
                .hasMessageContaining("Ticket not found")
                .isInstanceOf(ResourceNotFoundException.class);
    }
}