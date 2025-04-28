package com.ttt.cinevibe.repository;

import com.ttt.cinevibe.model.Comment;
import com.ttt.cinevibe.model.MovieReview;
import com.ttt.cinevibe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReview(MovieReview review);
    Page<Comment> findByReview(MovieReview review, Pageable pageable);
    List<Comment> findByUser(User user);
    Page<Comment> findByUser(User user, Pageable pageable);
    int countByReview(MovieReview review);
}