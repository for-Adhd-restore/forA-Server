package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.PostScrapFilterService;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.PostScrapFilter;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.persistence.repository.CommentRepository;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.domain.board.persistence.repository.PostScrapFilterRepository;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostScrapFilterServiceImpl implements PostScrapFilterService {

    private final PostScrapFilterRepository postScrapFilterRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void toggleScrap(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        User user = userService.getUser(userId);

        postScrapFilterRepository.findByPostIdAndUserId(postId, userId)
                .ifPresentOrElse(
                        scrap -> {
                            postScrapFilterRepository.delete(scrap);
                            post.decrementScrapCount();
                        },
                        () -> {
                            PostScrapFilter newScrap = PostScrapFilter.builder()
                                    .post(post)
                                    .user(user)
                                    .build();
                            postScrapFilterRepository.save(newScrap);
                            post.incrementScrapCount();
                        }
                );
        postRepository.save(post);
    }

    @Override
    public Page<PostScrapFilter> getScrapsByUserAndCategory(String userId, Category category, Pageable pageable, SortOption sortOption) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), getSortByOption(sortOption));
        return postScrapFilterRepository.findByUserIdAndCategory(userId, category, sortedPageable);
    }

    @Override
    public long getCommentCount(Long postId) {
        long commentCount = commentRepository.countByPostId(postId);
        long replyCount = commentRepository.countByParentCommentId(postId);
        return commentCount + replyCount;
    }

    @Transactional
    public boolean isUserScrappedPost(String userId, Long postId) {
        return postScrapFilterRepository.existsByUserIdAndPostId(userId, postId);
    }


    private Sort getSortByOption(SortOption option) {
        switch (option) {
            case NEWEST_FIRST:
                return Sort.by("post.createdAt").descending();
            case OLDEST_FIRST:
                return Sort.by("post.createdAt").ascending();
            case MOST_VIEWED:
                return Sort.by("post.viewCount").descending();
            case MOST_COMMENTED:
                return Sort.by("post.commentCount").descending();
            case MOST_LIKED:
                return Sort.by("post.likeCount").descending();
            default:
                return Sort.unsorted();
        }
    }
}
