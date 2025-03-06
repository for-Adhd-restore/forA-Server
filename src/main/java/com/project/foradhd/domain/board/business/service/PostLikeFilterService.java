package com.project.foradhd.domain.board.business.service;

import com.project.foradhd.domain.board.persistence.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostLikeFilterService {
    void toggleLike(String userId, Long postId);
    Page<Post> getLikedPostsByUser(String userId, Pageable pageable);
    boolean isUserLikedPost(String userId, Long postId);
}
