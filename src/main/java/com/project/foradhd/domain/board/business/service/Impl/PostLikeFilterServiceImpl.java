package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.PostLikeFilterService;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.entity.PostLikeFilter;
import com.project.foradhd.domain.board.persistence.repository.PostLikeFilterRepository;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.foradhd.global.exception.ErrorCode.INVALID_REQUEST;
import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_POST;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeFilterServiceImpl implements PostLikeFilterService {

    private final PostLikeFilterRepository postLikeFilterRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void toggleLike(String userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_POST));

        User user = userService.getUser(userId);

        if (postLikeFilterRepository.existsByUserIdAndPostId(userId, postId)) {
            postLikeFilterRepository.deleteByUserIdAndPostId(userId, postId);
            post.decrementLikeCount();
        } else {
            PostLikeFilter newLike = PostLikeFilter.builder()
                    .user(user)
                    .post(post)
                    .build();
            postLikeFilterRepository.save(newLike);
            post.incrementLikeCount();
        }
        postRepository.save(post);
    }

    public boolean isUserLikedPost(String userId, Long postId) {
        return postLikeFilterRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public Page<Post> getLikedPostsByUser(String userId, Pageable pageable) {
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException(INVALID_REQUEST);
        }

        return postLikeFilterRepository.findPostsLikedByUser(userId, pageable);
    }
}
