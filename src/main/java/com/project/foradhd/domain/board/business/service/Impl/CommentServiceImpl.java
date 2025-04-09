package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.CommentService;
import com.project.foradhd.domain.board.business.service.NotificationService;
import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.persistence.entity.CommentLikeFilter;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.SortOption;
import com.project.foradhd.domain.board.persistence.repository.CommentLikeFilterRepository;
import com.project.foradhd.domain.board.persistence.repository.CommentRepository;
import com.project.foradhd.domain.board.persistence.repository.PostRepository;
import com.project.foradhd.domain.board.web.dto.response.PostListResponseDto;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.project.foradhd.global.exception.ErrorCode.*;
import static org.springframework.data.jpa.repository.query.QueryUtils.applySorting;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentServiceImpl implements CommentService {

    private final UserService userService;
    private final NotificationService notificationService;
    private final CommentRepository commentRepository;
    private final CommentLikeFilterRepository commentLikeFilterRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
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
                .anonymous(comment.getAnonymous())
                .likeCount(comment.getLikeCount())
                .nickname(comment.getNickname())
                .profileImage(comment.getProfileImage())
                .childComments(childComments)
                .build();
    }
    @Override
    @Transactional
    public Comment createComment(Comment comment, String userId) {
        Long postId = comment.getPost().getId();
        boolean postExists = postRepository.existsById(postId);

        if (!postExists) {
            throw new BusinessException(ErrorCode.NOT_FOUND_POST);
        }
        UserProfile userProfile = userService.getUserProfile(userId);

        Comment.CommentBuilder commentBuilder = comment.toBuilder().user(User.builder().id(userId).build());

        if (comment.getAnonymous()) {
            String anonymousNickname = generateAnonymousNickname(comment.getPost().getId(), userId);
            String anonymousProfileImage = "image/default-profile.png";

            commentBuilder
                    .nickname(anonymousNickname)
                    .profileImage(anonymousProfileImage);
        } else {
            commentBuilder
                    .nickname(userProfile.getNickname())
                    .profileImage(userProfile.getProfileImage());
        }

        Comment savedComment = commentRepository.save(commentBuilder.build());

        // 🔔 알림 전송 (댓글 작성자와 게시글 작성자가 다를 경우에만)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (!post.getUser().getId().equals(userId)) {
            String message = "내 게시글에 새로운 댓글이 달렸어요: " + savedComment.getContent();
            notificationService.createNotification(post.getUser().getId(), message);
        }

        return savedComment;
    }


    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        int childCount = commentRepository.countByParentCommentId(commentId);
        if (childCount > 0) {
            // 대댓글이 존재하면 soft delete 처리
            commentRepository.softDeleteById(commentId);
        } else {
            // 대댓글이 없으면 진짜 삭제해도 OK (soft delete로 유지하고 싶다면 이 줄도 softDeleteById로 대체 가능)
            commentRepository.softDeleteById(commentId);
        }
    }

    @Override
    @Transactional
    public void deleteChildrenComment(Long commentId) {
        commentRepository.softDeleteCommentById(commentId);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, String content, boolean anonymous, String userId) {
        if (commentId == null || userId == null || userId.isEmpty()) {
            throw new BusinessException(INVALID_REQUEST);
        }
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND_COMMENT));
        // 댓글 작성자가 아니면 수정 불가
        if (!existingComment.getUser().getId().equals(userId)) {
            throw new BusinessException(ACCESS_DENIED);
        }

        // 댓글 수정
        Comment.CommentBuilder updatedCommentBuilder = existingComment.toBuilder()
                .content(content)
                .anonymous(anonymous);

        if (!anonymous) {
            UserProfile userProfile = userService.getUserProfile(userId);
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
    @Transactional
    public Page<Post> getMyCommentedPosts(String userId, Pageable pageable, SortOption sortOption) {
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException(NOT_FOUND_USER);
        }

        pageable = applySorting(pageable, sortOption);

        // 사용자가 작성한 댓글이 포함된 글 가져오기
        Page<Post> posts = postRepository.findByCommentsUserId(userId, pageable);

        if (posts.isEmpty()) {
            throw new BusinessException(NOT_FOUND_POST);
        }

        return posts;
    }

    @Override
    @Transactional
    public Page<Comment> getCommentsByPost(Long postId, Pageable pageable, SortOption sortOption) {
        pageable = applySorting(pageable, sortOption);
        return commentRepository.findTopLevelCommentsWithChildren(postId, pageable);
    }

    @Override
    @Transactional
    public void toggleCommentLike(Long commentId, String userId) {
        if (commentId == null || userId == null || userId.isEmpty()) {
            throw new BusinessException(INVALID_REQUEST);
        }

        if (!commentRepository.existsById(commentId)) {
            throw new BusinessException(NOT_FOUND_COMMENT);
        }
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

    @Override
    @Transactional(readOnly = true)
    public boolean isUserLikedComment(String userId, Long commentId) {
        return commentLikeFilterRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCommentAuthor(String userId, Long commentId) {
        return commentRepository.existsByIdAndUserId(commentId, userId);
    }
}
