package com.project.foradhd.domain.board.business.service;

import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    // 특정 게시글 조회
    Post getPost(Long Id);

    // 새 게시글 생성
    Post createPost(Post post);

    // 기존 게시글 업데이트
    Post updatePost(Post post);

    // 특정 카테고리의 게시글 목록 조회
    Page<Post> listByCategory(Category category, Pageable pageable, SortOption sortOption);

    // 게시글 삭제
    void deletePost(Long Id);

    // 모든 게시글 목록 조회
    Page<Post> getAllPosts(Pageable pageable);

    // 사용자가 작성한 게시글 목록 조회, 정렬 옵션 추가
    Page<Post> getUserPosts(String userId, Pageable pageable, SortOption sortOption);
    Page<Post> getUserPostsByCategory(String userId, Category category, Pageable pageable, SortOption sortOption);

    // 글 조회수 증가
    Post getAndIncrementViewCount(Long postId);
    // 메인홈 - 실시간 랭킹
    Page<Post> getTopPosts(Pageable pageable);

    // 메인홈 - 카테고리별 실시간 랭킹
    Page<Post> getTopPostsByCategory(Category category, Pageable pageable);
    // SSE 알림 관련 설정
    void addComment(Long postId, String commentContent, String userId);
    List<String> getRecentSearchTerms(String userId);
    Page<Post> searchPostsByTitle(String title, String userId, Pageable pageable);
}
