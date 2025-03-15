package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.CommentLikeFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface CommentLikeFilterRepository extends JpaRepository<CommentLikeFilter, Long> {
    Optional<CommentLikeFilter> findByCommentIdAndUserId(Long commentId, String userId);
    Optional<CommentLikeFilter> deleteByCommentIdAndUserId(Long commentId, String userId);
    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(Long commentId);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId")
    void decrementLikeCount(Long commentId);
    boolean existsByUserIdAndCommentId(String userId, Long commentId);
}
