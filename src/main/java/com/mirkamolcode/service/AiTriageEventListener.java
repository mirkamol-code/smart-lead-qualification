package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.event.CommentCreatedEvent;
import com.mirkamolcode.repository.CommentRepository;
import com.mirkamolcode.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AiTriageEventListener {

    private static final Logger log = LoggerFactory.getLogger(AiTriageEventListener.class);

    private final HuggingFaceService huggingFaceService;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public AiTriageEventListener(
            HuggingFaceService huggingFaceService,
            TicketRepository ticketRepository,
            CommentRepository commentRepository
    ) {
        this.huggingFaceService = huggingFaceService;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            log.info("Starting AI triage for comment id: {}", event.commentId());
            AiTicketResponse aiResponse = huggingFaceService.analyzeMessage(event.content());

            if (aiResponse != null && aiResponse.qualified()) {
                Comment comment = commentRepository.findById(event.commentId())
                        .orElseThrow(() -> new IllegalStateException("Comment not found for id: " + event.commentId()));

                Ticket ticket = new Ticket(
                        aiResponse.title(),
                        aiResponse.category(),
                        aiResponse.priority(),
                        aiResponse.summary(),
                        comment
                );
                ticketRepository.save(ticket);
                log.info("Ticket created successfully for comment id: {}", event.commentId());
            }
        } catch (Exception e) {
            log.error("Failed to triage comment id: {}. Error: {}", event.commentId(), e.getMessage(), e);
        }
    }
}
