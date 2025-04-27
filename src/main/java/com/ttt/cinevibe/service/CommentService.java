package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.request.CommentRequest;
import com.ttt.cinevibe.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    
    Page<CommentResponse> getReviewComments(Long reviewId, Pageable pageable);
    
    CommentResponse getCommentById(Long commentId);
    
    CommentResponse createComment(String userUid, CommentRequest request);
    
    CommentResponse updateComment(String userUid, Long commentId, CommentRequest request);
    
    void deleteComment(String userUid, Long commentId);
    
    CommentResponse likeComment(String userUid, Long commentId);
    
    CommentResponse unlikeComment(String userUid, Long commentId);
    
    int getCommentCountForReview(Long reviewId);
}