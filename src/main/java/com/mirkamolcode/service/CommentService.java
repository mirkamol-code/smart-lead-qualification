package com.mirkamolcode.service;

import com.mirkamolcode.dto.AiTicketResponse;
import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.entity.Ticket;
import com.mirkamolcode.entity.enums.Category;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.CommentRepository;
import com.mirkamolcode.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationUserRepository userRepository;
    private final HuggingFaceService huggingFaceService;

    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository, ApplicationUserRepository userRepository, HuggingFaceService huggingFaceService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.huggingFaceService = huggingFaceService;
    }

    @Transactional
    public CommentResponse save(
            CreateCommentRequest request,
            String currentUsername
    ) {
        ApplicationUser currentUser = userRepository.findApplicationUserByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));
        Comment comment = new Comment();

        comment.setUser(currentUser);
        comment.setSenderEmail(request.senderEmail());
        comment.setContent(request.content());

        Comment savedComment =
                commentRepository.save(comment);

        processAiTriage(savedComment);

        return mapToCommentResponse(savedComment);
    }

    private void processAiTriage(Comment savedComment) {
        AiTicketResponse aiResponse =
                huggingFaceService.analyzeMessage(
                        savedComment.getContent()
                );

        if (aiResponse.qualified()) {

            Ticket ticket = new Ticket();

            ticket.setTitle(aiResponse.title());

            ticket.setCategory(aiResponse.category());

            ticket.setPriority(aiResponse.priority());

            ticket.setSummary(aiResponse.summary());

            ticket.setComment(savedComment);


            ticketRepository.save(ticket);
        }
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(String username, Pageable pageable) {
        return commentRepository.findAllByUserUsername(username, pageable)
                .map(this::mapToCommentResponse);
    }

    @Transactional(readOnly = true)
    public CommentResponse getAnyCommentById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        return mapToCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public CommentResponse getOwnCommentById(UUID id, UserDetails userDetails) {
        Comment comment = commentRepository.findByIdAndUserUsername(id, userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        return mapToCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getAll(Pageable pageable) {
        return commentRepository
                .findAll(pageable)
                .map(this::mapToCommentResponse);
    }


    private CommentResponse mapToCommentResponse(
            Comment comment
    ) {

        return new CommentResponse(
                comment.getId(),
                comment.getSenderEmail(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
