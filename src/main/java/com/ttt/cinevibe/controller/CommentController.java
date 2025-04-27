package com.ttt.cinevibe.controller;

import com.ttt.cinevibe.dto.request.CommentRequest;
import com.ttt.cinevibe.dto.response.CommentResponse;
import com.ttt.cinevibe.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment API", description = "Endpoints to manage comments on movie reviews")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get comments for a review", description = "Returns all comments for a specific review")
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Page<CommentResponse>> getReviewComments(
            @PathVariable Long reviewId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(commentService.getReviewComments(reviewId, pageable));
    }

    @Operation(summary = "Get a comment by ID", description = "Returns a specific comment")
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @Operation(summary = "Create a new comment", description = "Adds a comment to a review")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        CommentResponse created = commentService.createComment(userUid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a comment", description = "Updates an existing comment owned by the authenticated user")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(commentService.updateComment(userUid, commentId, request));
    }

    @Operation(summary = "Delete a comment", description = "Deletes a comment owned by the authenticated user")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String userUid = authentication.getName();
        commentService.deleteComment(userUid, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Like a comment", description = "Adds a like to a comment")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentResponse> likeComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(commentService.likeComment(userUid, commentId));
    }

    @Operation(summary = "Unlike a comment", description = "Removes a like from a comment")
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<CommentResponse> unlikeComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String userUid = authentication.getName();
        return ResponseEntity.ok(commentService.unlikeComment(userUid, commentId));
    }
}