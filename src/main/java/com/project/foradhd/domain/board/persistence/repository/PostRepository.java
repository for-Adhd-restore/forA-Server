package com.project.foradhd.domain.board.persistence.repository;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 📌 개별 게시글 조회 (유저 프로필 포함)
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    Optional<Post> findById(@Param("postId") Long postId);

    // 📌 카테고리별 게시글 조회 (페이징 적용)
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    Page<Post> findByCategory(@Param("category") Category category, Pageable pageable);

    // 📌 특정 유저가 작성한 게시글 조회
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    Page<Post> findByUserId(@Param("userId") String userId, Pageable pageable);

     // 📌 특정 유저가 작성한 카테고리별 게시글 조회
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    Page<Post> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") Category category, Pageable pageable);

    // 📌 인기 게시글 조회 (상위 10개)
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    @Query("""
        SELECT p FROM Post p 
        ORDER BY p.viewCount DESC
        """)
    Page<Post> findTopPosts(Pageable pageable);

     // 📌 카테고리별 인기 게시글 조회 (상위 10개)
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    @Query("""
        SELECT p FROM Post p 
        WHERE p.category = :category
        ORDER BY p.viewCount DESC
        """)
    Page<Post> findTopPostsByCategory(@Param("category") Category category, Pageable pageable);

     // 📌 제목으로 게시글 검색 (페이징 적용)
    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    Page<Post> findByTitleContaining(@Param("title") String title, Pageable pageable);

    Page<Post> findAllByUserId(String userId, Pageable pageable);

    Page<Post> findAllByUserIdAndCategory(String userId, Category category, Pageable pageable);

    @Query("""
    SELECT p FROM Post p 
    WHERE p.user.id = :userId
    ORDER BY 
        CASE 
            WHEN :sortOption = 'NEWEST_FIRST' THEN p.createdAt 
            WHEN :sortOption = 'OLDEST_FIRST' THEN p.createdAt 
            WHEN :sortOption = 'MOST_VIEWED' THEN p.viewCount 
            WHEN :sortOption = 'MOST_COMMENTED' THEN SIZE(p.comments) 
            WHEN :sortOption = 'MOST_LIKED' THEN p.likeCount 
            ELSE p.createdAt 
        END 
        DESC
""")
    Page<Post> findAllByUserId(
            @Param("userId") String userId,
            @Param("sortOption") String sortOption,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Post p 
    WHERE p.user.id = :userId
    AND (:category IS NULL OR p.category = :category)
    ORDER BY 
        CASE 
            WHEN :sortOption = 'NEWEST_FIRST' THEN p.createdAt
            WHEN :sortOption = 'OLDEST_FIRST' THEN p.createdAt
            WHEN :sortOption = 'MOST_VIEWED' THEN p.viewCount
            WHEN :sortOption = 'MOST_COMMENTED' THEN SIZE(p.comments)
            WHEN :sortOption = 'MOST_LIKED' THEN p.likeCount
            ELSE p.createdAt
        END DESC
""")
    Page<Post> findAllByUserIdAndCategoryWithSort(
            @Param("userId") String userId,
            @Param("category") Category category,
            @Param("sortOption") String sortOption,
            Pageable pageable
    );

}