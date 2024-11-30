package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.NotificationService;
import com.project.foradhd.domain.board.business.service.PostSearchHistoryService;
import com.project.foradhd.domain.board.business.service.PostService;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.util.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.project.foradhd.global.exception.ErrorCode.BOARD_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostSearchHistoryService postSearchHistoryService;
    private final NotificationService notificationService;
    private final SseEmitters sseEmitters;

    @Override
    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));
    }

    @Override
    @Transactional
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Post post) {
        Post existingPost = getPost(post.getId());

        Post updatedPost = existingPost.toBuilder()
                .title(post.getTitle())
                .content(post.getContent())
                .images(post.getImages())
                .anonymous(post.getAnonymous())
                .build();

        return postRepository.save(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // 나의 글
    @Override
    public Page<Post> getUserPosts(String userId, Pageable pageable, SortOption sortOption) {
        pageable = applySorting(pageable, sortOption);
        return postRepository.findByUserIdWithUserProfile(userId, pageable);
    }

    @Override
    public Page<Post> getUserPostsByCategory(String userId, Category category, Pageable pageable, SortOption sortOption) {
        pageable = applySorting(pageable, sortOption);
        return postRepository.findByUserIdAndCategoryWithUserProfile(userId, category, pageable);
    }

    // 글 카테고리별 정렬
    @Override
    public Page<Post> listByCategory(Category category, Pageable pageable) {
        return postRepository.findByCategoryWithUserProfile(category, pageable);
    }

    // 글 조회수 증가
    @Override
    @Transactional
    public Post getAndIncrementViewCount(Long postId) {
        Post post = getPost(postId);
        post.incrementViewCount();
        return post;
    }

    @Override
    @Transactional
    public Page<Post> getTopPosts(Pageable pageable) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);
        Page<Post> topPosts = postRepository.findTopPostsWithUserProfile(pageRequest);
        notifyUsersAboutTopPosts(topPosts.getContent());
        return topPosts;
    }

    @Override
    @Transactional
    public Page<Post> getTopPostsByCategory(Category category, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), 10);
        Page<Post> topPosts = postRepository.findTopPostsByCategoryWithUserProfile(category, pageRequest);
        notifyUsersAboutTopPosts(topPosts.getContent());
        return topPosts;
    }

    private void notifyUsersAboutTopPosts(List<Post> topPosts) {
        for (Post post : topPosts) {
            String message = "내 글이 TOP 10 게시물로 선정됐어요!";
            notificationService.createNotification(post.getUser().getId(), message);
            sseEmitters.sendNotification(post.getUser().getId(), message);
        }
    }

    @Override
    public void addComment(Long postId, String commentContent, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(BOARD_NOT_FOUND));

        String message = "새로운 댓글이 달렸어요: " + commentContent;
        notificationService.createNotification(post.getUser().getId(), message);
        sseEmitters.sendNotification(post.getUser().getId(), message);
    }

    private Pageable applySorting(Pageable pageable, SortOption sortOption) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        switch (sortOption) {
            case NEWEST_FIRST:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case OLDEST_FIRST:
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case MOST_VIEWED:
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
            case MOST_LIKED:
                sort = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            default:
                break;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public List<String> getRecentSearchTerms(String userId) {
        return postSearchHistoryService.getRecentSearchTerms(userId);
    }

    @Override
    @Transactional
    public Page<Post> searchPostsByTitle(String title, String userId, Pageable pageable) {
        // 검색어 저장 로직 추가
        postSearchHistoryService.saveSearchTerm(userId, title);
        return postRepository.findByTitleContainingWithUserProfile(title, pageable);
    }
}
