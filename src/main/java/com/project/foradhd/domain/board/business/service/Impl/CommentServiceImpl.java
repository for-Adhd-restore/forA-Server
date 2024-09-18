package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.CommentService;
import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.persistence.entity.CommentLikeFilter;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.persistence.repository.CommentLikeFilterRepository;
import com.project.foradhd.domain.board.persistence.repository.CommentRepository;
import com.project.foradhd.domain.board.web.dto.request.CreateCommentRequestDto;
import com.project.foradhd.domain.board.web.dto.response.PostResponseDto;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.domain.user.persistence.repository.UserProfileRepository;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_COMMENT;
import static com.project.foradhd.global.exception.ErrorCode.NOT_FOUND_USER_PROFILE;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeFilterRepository commentLikeFilterRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Comment getComment(Long commentId) {
        Comment comment = commentRepository.findByIdFetch(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        List<Comment> childComments = comment.getChildComments();

        return Comment.builder()
                .id(comment.getId())
                .post(comment.getPost())
                .user(comment.getUser())
                .content(comment.getContent())
                .parentComment(comment.getParentComment())
                .anonymous(comment.isAnonymous())
                .likeCount(comment.getLikeCount())
                .nickname(comment.getNickname())
                .profileImage(comment.getProfileImage())
                .childComments(childComments)
                .build();
    }
    @Override
    @Transactional
    public Comment createComment(Comment comment, String userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        Comment.CommentBuilder commentBuilder = comment.toBuilder().user(User.builder().id(userId).build());

        if (comment.isAnonymous()) {
            String anonymousNickname = generateAnonymousNickname(comment.getPost().getId(), userId);
            String anonymousProfileImage = "http://example.com/anonymous-profile.png"; // 지정한 URL

            commentBuilder
                    .nickname(anonymousNickname)
                    .profileImage(anonymousProfileImage);
        } else {
            commentBuilder
                    .nickname(userProfile.getNickname())
                    .profileImage(userProfile.getProfileImage());
        }

        return commentRepository.save(commentBuilder.build());
    }


    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        // 대댓글의 부모 참조를 한 번의 배치 업데이트로 null 처리
        commentRepository.detachChildComments(commentId);

        // 원댓글 삭제
        commentRepository.deleteCommentById(commentId);
    }

    @Transactional
    public void deleteChildrenComment(Long commentId) {
        commentRepository.deleteByParentId(commentId);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String content, boolean anonymous, String userId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_COMMENT));

        // 댓글 수정
        Comment.CommentBuilder updatedCommentBuilder = existingComment.toBuilder()
                .content(content)
                .anonymous(anonymous);

        if (!anonymous) {
            UserProfile userProfile = userProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(NOT_FOUND_USER_PROFILE));
            updatedCommentBuilder.nickname(userProfile.getNickname())
                    .profileImage(userProfile.getProfileImage());
        } else {
            updatedCommentBuilder.nickname(null) // 익명일 경우 닉네임 초기화
                    .profileImage(null); // 익명일 경우 프로필 이미지 초기화
        }

        Comment updatedComment = updatedCommentBuilder.build();
        return commentRepository.save(updatedComment);
    }

    @Override
    public Page<PostResponseDto.PostListResponseDto> getMyCommentedPosts(String userId, Pageable pageable, SortOption sortOption) {
        Sort sort = switch (sortOption) {
            case OLDEST_FIRST -> Sort.by(Sort.Direction.ASC, "createdAt");
            case NEWEST_FIRST -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Comment> userComments = commentRepository.findByUserId(userId, sortedPageable);
        List<PostResponseDto.PostListResponseDto> posts = userComments.stream()
                .map(Comment::getPost)
                .distinct()
                .map(post -> PostResponseDto.PostListResponseDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(posts, sortedPageable, userComments.getTotalElements());
    }

    @Override
    @Transactional
    public Page<Comment> getCommentsByPost(Long postId, Pageable pageable, SortOption sortOption) {
        pageable = applySorting(pageable, sortOption);
        return commentRepository.findByPostId(postId, pageable);
    }

    @Override
    @Transactional
    public void toggleCommentLike(Long commentId, String userId) {
        Optional<CommentLikeFilter> likeFilter = commentLikeFilterRepository.findByCommentIdAndUserId(commentId, userId);
        if (likeFilter.isPresent()) {
            commentLikeFilterRepository.deleteByCommentIdAndUserId(commentId, userId);
            commentLikeFilterRepository.decrementLikeCount(commentId);
        } else {
            CommentLikeFilter newLikeFilter = CommentLikeFilter.builder()
                    .comment(Comment.builder().id(commentId).build())
                    .user(User.builder().id(userId).build())
                    .build();
            commentLikeFilterRepository.save(newLikeFilter);
            commentLikeFilterRepository.incrementLikeCount(commentId);
        }
    }

    private Pageable applySorting(Pageable pageable, SortOption sortOption) {
        Sort sort = switch (sortOption) {
            case OLDEST_FIRST -> Sort.by(Sort.Direction.ASC, "createdAt");
            case NEWEST_FIRST, MOST_LIKED, MOST_VIEWED -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.unsorted();
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public String generateAnonymousNickname(Long postId, String userId) {
        List<Comment> userComments = commentRepository.findByPostIdAndUserIdAndAnonymous(postId, userId, true);
        if (!userComments.isEmpty()) {
            return userComments.get(0).getNickname();
        } else {
            long anonymousCount = commentRepository.countByPostIdAndAnonymous(postId, true);
            return "익명 " + (anonymousCount + 1);
        }
    }
}
