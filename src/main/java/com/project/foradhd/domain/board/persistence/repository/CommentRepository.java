package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
    Page<Comment> findByUserId(@Param("userId") String userId, Pageable pageable);

    // ✅ 소프트 삭제로 변경
    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.content = '삭제된 댓글입니다.', c.nickname = '(삭제)', c.profileImage = 'image/default-profile.png' WHERE c.id = :commentId")
    void softDeleteById(@Param("commentId") Long commentId);

    // ✅ 대댓글도 soft delete로 유지하고 싶다면 이도 변경 가능
    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.content = '삭제된 댓글입니다.', c.nickname = '(삭제)', c.profileImage = 'image/default-profile.png' WHERE c.id = :id")
    void softDeleteCommentById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.parentComment = null WHERE c.parentComment.id = :parentId")
    void detachChildComments(Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :parentId")
    int countByParentCommentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    long countByPostIdAndAnonymous(Long postId, boolean anonymous);

    List<Comment> findByPostIdAndUserIdAndAnonymous(Long postId, String userId, boolean anonymous);

    @EntityGraph(attributePaths = {"childComments"})
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId")
    Optional<Comment> findByIdFetch(@Param("commentId") Long commentId);

    boolean existsByIdAndUserId(Long id, String userId);

    // ✅ 삭제되지 않은 원댓글만 조회
    @EntityGraph(attributePaths = {
            "user",
            "user.userProfile",
            "childComments",
            "childComments.user",
            "childComments.user.userProfile"
    })
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.post.id = :postId 
        AND c.parentComment IS NULL 
    """)
    Page<Comment> findTopLevelCommentsWithChildren(@Param("postId") Long postId, Pageable pageable);
}
