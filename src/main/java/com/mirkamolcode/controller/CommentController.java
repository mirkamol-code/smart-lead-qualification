package com.mirkamolcode.controller;

import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('comment:create')")
    public CommentResponse create(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return commentService.save(request, userDetails.getUsername());
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('comment:read')")
    public Page<CommentResponse> getMyComments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        return commentService.getMyComments(userDetails.getUsername(), pageable);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('commentList:read')")
    public Page<CommentResponse> getAll(Pageable pageable) {
        return commentService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('comment:read')")
    public CommentResponse getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return commentService.getById(id, userDetails);
    }
}
