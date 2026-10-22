package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.entity.enums.Priority;
import com.mirkamolcode.event.CommentCreatedEvent;
import com.mirkamolcode.repository.CommentRepository;
import com.mirkamolcode.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTriageEventListenerTest {

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AiTriageEventListener underTest;

    @Test
    void shouldCreateTicketWhenQualified() {
        // given
        UUID commentId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(commentId, "Payment failed");
        Comment comment = new Comment("user@gmail.com", "Payment failed");
        comment.setId(commentId);

        AiTicketResponse aiResponse = new AiTicketResponse(
                true,
                "Payment Failure",
                Category.BILLING,
                Priority.HIGH,
                "Customer cannot pay"
        );

        given(huggingFaceService.analyzeMessage("Payment failed")).willReturn(aiResponse);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        underTest.handleCommentCreated(event);

        // then
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldNotCreateTicketWhenNotQualified() {
        // given
        UUID commentId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(commentId, "Hello");

        AiTicketResponse aiResponse = new AiTicketResponse(
                false,
                null,
                null,
                null,
                null
        );

        given(huggingFaceService.analyzeMessage("Hello")).willReturn(aiResponse);

        // when
        underTest.handleCommentCreated(event);

        // then
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(commentRepository, never()).findById(any(UUID.class));
    }

    @Test
    void shouldHandleAiFailureGracefullyWithoutThrowing() {
        // given
        UUID commentId = UUID.randomUUID();
        CommentCreatedEvent event = new CommentCreatedEvent(commentId, "Payment failed");

        given(huggingFaceService.analyzeMessage("Payment failed"))
                .willThrow(new RuntimeException("HuggingFace API timeout"));

        // when
        underTest.handleCommentCreated(event);

        // then - no exception is propagated
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}
