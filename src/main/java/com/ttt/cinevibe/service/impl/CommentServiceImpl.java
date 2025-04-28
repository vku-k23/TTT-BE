package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.request.CommentRequest;
import com.ttt.cinevibe.dto.response.CommentResponse;
import com.ttt.cinevibe.dto.response.UserResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.exception.UnauthorizedException;
import com.ttt.cinevibe.model.Comment;
import com.ttt.cinevibe.model.MovieReview;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.repository.CommentRepository;
import com.ttt.cinevibe.repository.MovieReviewRepository;
import com.ttt.cinevibe.service.CommentService;
import com.ttt.cinevibe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final MovieReviewRepository reviewRepository;
    private final UserService userService;

    @Override
    public Page<CommentResponse> getReviewComments(Long reviewId, Pageable pageable) {
        MovieReview review = getReviewOrThrow(reviewId);
        return commentRepository.findByReview(review, pageable)
                .map(this::mapToCommentResponse);
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        return mapToCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse createComment(String userUid, CommentRequest request) {
        User user = getUserOrThrow(userUid);
        MovieReview review = getReviewOrThrow(request.getReviewId());
        
        Comment comment = Comment.builder()
                .user(user)
                .review(review)
                .content(request.getContent())
                .likesCount(0)
                .build();
        
        comment = commentRepository.save(comment);
        log.info("Created new comment with ID: {} for review: {} by user: {}", 
                comment.getId(), request.getReviewId(), userUid);
        
        return mapToCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(String userUid, Long commentId, CommentRequest request) {
        Comment comment = getCommentOrThrow(commentId);
        
        // Verify the comment belongs to the user
        if (!comment.getUser().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to update this comment");
        }
        
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);
        
        log.info("Updated comment with ID: {} by user: {}", commentId, userUid);
        return mapToCommentResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(String userUid, Long commentId) {
        Comment comment = getCommentOrThrow(commentId);
        
        // Verify the comment belongs to the user
        if (!comment.getUser().getFirebaseUid().equals(userUid)) {
            throw new UnauthorizedException("You don't have permission to delete this comment");
        }
        
        commentRepository.delete(comment);
        log.info("Deleted comment with ID: {} by user: {}", commentId, userUid);
    }

    @Override
    @Transactional
    public CommentResponse likeComment(String userUid, Long commentId) {
        // In a real-world scenario, you'd track which users liked which comments
        // Here we're just incrementing the counter for simplicity
        Comment comment = getCommentOrThrow(commentId);
        comment.setLikesCount(comment.getLikesCount() + 1);
        comment = commentRepository.save(comment);
        
        log.info("User {} liked comment {}", userUid, commentId);
        return mapToCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse unlikeComment(String userUid, Long commentId) {
        // In a real-world scenario, you'd track which users liked which comments
        // Here we're just decrementing the counter for simplicity
        Comment comment = getCommentOrThrow(commentId);
        
        // Ensure likes count doesn't go below 0
        int currentLikes = comment.getLikesCount();
        if (currentLikes > 0) {
            comment.setLikesCount(currentLikes - 1);
            comment = commentRepository.save(comment);
            log.info("User {} unliked comment {}", userUid, commentId);
        }
        
        return mapToCommentResponse(comment);
    }

    @Override
    public int getCommentCountForReview(Long reviewId) {
        MovieReview review = getReviewOrThrow(reviewId);
        return commentRepository.countByReview(review);
    }
    
    private User getUserOrThrow(String userUid) {
        UserResponse user = userService.findByFirebaseUid(userUid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with UID: " + userUid);
        }
        return mapToUser(user);
    }
    
    private MovieReview getReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
    }
    
    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
    }
    
    private CommentResponse mapToCommentResponse(Comment comment) {
        User user = comment.getUser();
        return CommentResponse.builder()
                .id(comment.getId())
                .reviewId(comment.getReview().getId())
                .userUid(user.getFirebaseUid())
                .userName(user.getDisplayName())
                .userProfileImageUrl(user.getProfileImageUrl())
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private User mapToUser(UserResponse userResponse) {
        return User.builder()
                .firebaseUid(userResponse.getFirebaseUid())
                .email(userResponse.getEmail())
                .displayName(userResponse.getDisplayName())
                .profileImageUrl(userResponse.getProfileImageUrl())
                .createdAt(userResponse.getCreatedAt())
                .lastLogin(userResponse.getLastLogin())
                .build();
    }
}