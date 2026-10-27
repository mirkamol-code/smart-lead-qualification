package com.mirkamolcode.service;

import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Comment;
import com.mirkamolcode.event.CommentCreatedEvent;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ApplicationUserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(CommentRepository commentRepository,
                          ApplicationUserRepository userRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        eventPublisher.publishEvent(
                new CommentCreatedEvent(savedComment.getId(), savedComment.getContent())
        );

        return mapToCommentResponse(savedComment);
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
