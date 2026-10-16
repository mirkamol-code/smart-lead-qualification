package com.mirkamolcode.controller;

import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.dto.CommentResponse;
import com.mirkamolcode.dto.CreateCommentRequest;
import com.mirkamolcode.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<CommentResponse> create(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentResponse response = commentService.save(request, userDetails.getUsername());
        return ApiResponse.success("Comment created successfully", 201, response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('comment:read')")
    public ApiResponse<Page<CommentResponse>> getMyComments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Page<CommentResponse> comments = commentService.getMyComments(userDetails.getUsername(), pageable);
        return ApiResponse.success("User comments retrieved successfully", comments);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('commentList:read')")
    public ApiResponse<Page<CommentResponse>> getAll(Pageable pageable) {
        Page<CommentResponse> comments = commentService.getAll(pageable);
        return ApiResponse.success("All comments retrieved successfully", comments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('comment:read')")
    public ApiResponse<CommentResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentResponse comment = commentService.getById(id, userDetails);
        return ApiResponse.success("Comment retrieved successfully", comment);
    }
}
