package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.PostScrapFilter;
import com.project.foradhd.domain.board.persistence.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostScrapFilterRepository extends JpaRepository<PostScrapFilter, Long> {
    Optional<PostScrapFilter> findByPostIdAndUserId(Long postId, String userId);
    Page<PostScrapFilter> findByUserId(String userId, Pageable pageable);
    @Query("SELECT p FROM PostScrapFilter p WHERE p.user.id = :userId AND p.post.category = :category")
    Page<PostScrapFilter> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") Category category, Pageable pageable);
    boolean existsByUserIdAndPostId(String userId, Long postId);
}
