package com.project.foradhd.domain.board.web.mapper;

import com.project.foradhd.domain.board.business.service.CommentService;
import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.web.dto.request.CreateCommentRequestDto;
import com.project.foradhd.domain.board.web.dto.response.CommentListResponseDto;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import com.project.foradhd.domain.user.persistence.repository.UserProfileRepository;
import com.project.foradhd.domain.user.persistence.repository.UserRepository;
import org.hibernate.validator.constraints.br.CPF;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "postId", target = "post", qualifiedByName = "mapPost")
    @Mapping(source = "parentCommentId", target = "parentComment", qualifiedByName = "mapParentComment")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "childComments", ignore = true)
    @Mapping(target = "likeCount", constant = "0")
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "user", ignore = true)
    Comment createCommentRequestDtoToComment(CreateCommentRequestDto createCommentRequestDto, @Context String userId);

    @AfterMapping
    default void setUserProfileFields(@MappingTarget Comment.CommentBuilder commentBuilder, @Context String userId, @Context UserProfileRepository userProfileRepository, CreateCommentRequestDto createCommentRequestDto) {
        if (!createCommentRequestDto.isAnonymous()) {
            UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
            if (userProfile != null) {
                commentBuilder.nickname(userProfile.getNickname());
                commentBuilder.profileImage(userProfile.getProfileImage());
            }
        }
        commentBuilder.user(User.builder().id(userId).build());
    }

    @Mapping(source = "comment.id", target = "id")
    @Mapping(source = "comment.content", target = "content")
    @Mapping(source = "comment.user.id", target = "userId")
    @Mapping(source = "comment.post.id", target = "postId")
    @Mapping(source = "comment.anonymous", target = "anonymous")
    @Mapping(source = "comment.likeCount", target = "likeCount")
    @Mapping(source = "comment.createdAt", target = "createdAt")
    @Mapping(source = "comment.lastModifiedAt", target = "lastModifiedAt")
    @Mapping(source = "comment.parentComment.id", target = "parentCommentId")
    @Mapping(target = "children", expression = "java(mapChildComments(comment.getChildComments(), blockedUserIdList, loggedInUserId, userService, commentService))")
    @Mapping(target = "nickname", expression = "java(getNickname(comment, userService))")
    @Mapping(target = "profileImage", expression = "java(getProfileImage(comment, userService))")
    @Mapping(target = "isBlocked", expression = "java(isBlockedUser(comment, blockedUserIdList))")
    @Mapping(source = "isLiked", target = "isLiked")
    @Mapping(source = "isCommentAuthor", target = "isCommentAuthor")
    CommentListResponseDto.CommentResponseDto commentToCommentResponseDto(
            Comment comment,
            List<String> blockedUserIdList,
            boolean isLiked,
            boolean isCommentAuthor,
            String loggedInUserId,
            UserService userService,
            @Context CommentService commentService);

    // ✅ 댓글 작성자인지 확인하는 함수
    default boolean isCommentAuthor(Comment comment, String loggedInUserId) {
        return comment.getUser() != null && comment.getUser().getId().equals(loggedInUserId);
    }


    // ✅ 닉네임 가져오는 함수
    default String getNickname(Comment comment, UserService userService) {
        if (comment.getAnonymous()) {
            return "익명";
        }
        if (comment.getUser() != null) {
            UserProfile userProfile = userService.getUserProfile(comment.getUser().getId());
            if (userProfile != null && userProfile.getNickname() != null) {
                return userProfile.getNickname();
            }
        }
        return "알 수 없음";
    }

    // ✅ 프로필 이미지 가져오는 함수
    default String getProfileImage(Comment comment, UserService userService) {
        if (comment.getAnonymous()) {
            return "image/default-profile.png";
        }
        if (comment.getUser() != null) {
            UserProfile userProfile = userService.getUserProfile(comment.getUser().getId());
            if (userProfile != null && userProfile.getProfileImage() != null) {
                return userProfile.getProfileImage();
            }
        }
        return "image/default-profile.png";
    }

    // ✅ 차단된 사용자 여부 확인 함수
    default boolean isBlockedUser(Comment comment, List<String> blockedUserIdList) {
        return comment.getUser() != null && blockedUserIdList.contains(comment.getUser().getId());
    }

    @Named("mapChildComments")
    default List<CommentListResponseDto.CommentResponseDto> mapChildComments(
            List<Comment> childComments,
            List<String> blockedUserIdList,
            String loggedInUserId,
            UserService userService,
            @Context CommentService commentService) {
        if (childComments == null) {
            return List.of();
        }

        return childComments.stream()
                .map(childComment -> {
                    boolean isLiked = commentService.isUserLikedComment(loggedInUserId, childComment.getId()); // ✅ 각 childComment의 isLiked 조회
                    boolean isCommentAuthor = commentService.isCommentAuthor(loggedInUserId, childComment.getId()); // ✅ 각 childComment의 작성자인지 조회

                    return commentToCommentResponseDto(childComment, blockedUserIdList, isLiked, isCommentAuthor, loggedInUserId, userService, commentService);
                })
                .toList();
    }


    default CommentListResponseDto.CommentResponseDto commentToCommentListResponseDtoWithChildren(
            Comment comment,
            List<String> blockedUserIdList,
            String loggedInUserId,
            @Context UserService userService,
            @Context CommentService commentService) {

        boolean isCommentAuthor = commentService.isCommentAuthor(loggedInUserId, comment.getId()); // ✅ 서비스에서 조회
        boolean isLiked = commentService.isUserLikedComment(loggedInUserId, comment.getId()); // ✅ 서비스에서 조회
        boolean isBlocked = comment.getUser() != null && blockedUserIdList.contains(comment.getUser().getId());

        return CommentListResponseDto.CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .postId(comment.getPost().getId())
                .anonymous(comment.getAnonymous())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .lastModifiedAt(comment.getLastModifiedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .children(comment.getChildComments() != null ? mapChildComments(comment.getChildComments(), blockedUserIdList, loggedInUserId, userService, commentService) : List.of()) // ✅ 변경
                .nickname(getNickname(comment, userService))
                .profileImage(getProfileImage(comment, userService))
                .isBlocked(isBlocked)
                .isCommentAuthor(isCommentAuthor) // ✅ 추가
                .isLiked(isLiked) // ✅ 추가
                .build();
    }

    @Named("mapPost")
    static Post mapPost(Long postId) {
        if (postId == null) {
            return null;
        }
        return Post.builder().id(postId).build();
    }

    @Named("mapParentComment")
    static Comment mapParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }
        return Comment.builder().id(parentCommentId).build();
    }

    @Named("mapUser")
    static User mapUser(String userId, @Context UserRepository userRepository) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }
}