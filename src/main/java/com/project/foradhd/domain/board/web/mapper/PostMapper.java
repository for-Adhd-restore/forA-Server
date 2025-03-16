package com.project.foradhd.domain.board.web.mapper;

import com.project.foradhd.domain.board.business.service.CommentService;
import com.project.foradhd.domain.board.business.service.dto.in.ReportPostData;
import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.web.dto.PostDto;
import com.project.foradhd.domain.board.web.dto.request.PostRequestDto;
import com.project.foradhd.domain.board.web.dto.response.CommentListResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostListResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostRankingResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostReportListResponseDto.PostReportResponseDto;
import com.project.foradhd.domain.board.web.dto.response.PostSearchResponseDto;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Context;

import org.mapstruct.factory.Mappers;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface PostMapper {

    @Mapping(source = "category", target = "category")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "profileImage", target = "profileImage")
    PostDto toDto(Post post);

    @Mapping(source = "category", target = "category")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "scrapCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Post toEntity(PostRequestDto dto, @Context String userId, @Context UserService userService);

    @AfterMapping
    default void setUser(@MappingTarget Post.PostBuilder postBuilder, @Context String userId, @Context UserService userService) {
        if (userId != null) {
            User user = userService.getUser(userId);
            UserProfile userProfile = userService.getUserProfile(userId);

            postBuilder.user(user);
            if (userProfile != null) {
                postBuilder.nickname(userProfile.getNickname());
                postBuilder.profileImage(userProfile.getProfileImage());
            }
        }
    }

    @Mapping(source = "post.id", target = "id")
    @Mapping(source = "post.user.id", target = "userId")
    @Mapping(source = "post.title", target = "title")
    @Mapping(source = "post.content", target = "content")
    @Mapping(source = "post.anonymous", target = "anonymous")
    @Mapping(source = "post.images", target = "images")
    @Mapping(source = "post.likeCount", target = "likeCount")
    @Mapping(source = "post.commentCount", target = "commentCount")
    @Mapping(source = "post.scrapCount", target = "scrapCount")
    @Mapping(source = "post.viewCount", target = "viewCount")
    @Mapping(source = "post.category", target = "category")
    @Mapping(target = "comments", ignore = true)
    //@Mapping(target = "comments", expression = "java(mapCommentList(post.getComments(), blockedUserIdList, loggedInUserId, userService, commentService))")
    @Mapping(source = "isScrapped", target = "isScrapped")
    @Mapping(source = "isLiked", target = "isLiked")
    @Mapping(source = "isAuthor", target = "isAuthor")
    @Mapping(target = "nickname", expression = "java(getNickname(post, userService))")
    @Mapping(target = "profileImage", expression = "java(getProfileImage(post, userService))")
    @Mapping(target = "isBlocked", expression = "java(isBlockedUser(post, blockedUserIdList))")
    PostListResponseDto.PostResponseDto toPostResponseDto(
            Post post,
            UserService userService,
            List<String> blockedUserIdList,
            boolean isScrapped,
            boolean isLiked,
            boolean isAuthor,
            String loggedInUserId,
            CommentService commentService
    );
    @AfterMapping
    default void setComments(
            @MappingTarget PostListResponseDto.PostResponseDto.PostResponseDtoBuilder dtoBuilder,
            Post post,
            @Context UserService userService,
            @Context CommentService commentService,
            @Context CommentMapper commentMapper,
            @Context List<String> blockedUserIdList,
            @Context String loggedInUserId
    ) {
        List<CommentListResponseDto.CommentResponseDto> mappedComments = post.getComments().stream()
                .filter(comment -> comment.getParentComment() == null) // 부모 댓글만 반환
                .map(comment -> {
                    boolean isLiked = commentService.isUserLikedComment(loggedInUserId, comment.getId());
                    boolean isCommentAuthor = commentService.isCommentAuthor(loggedInUserId, comment.getId());

                    return commentMapper.commentToCommentResponseDto(
                            comment, blockedUserIdList, isLiked, isCommentAuthor, loggedInUserId, userService, commentService
                    );
                })
                .collect(Collectors.toList());

        dtoBuilder.comments(mappedComments);
    }


    // ✅ 닉네임 가져오는 함수
    default String getNickname(Post post, UserService userService) {
        if (post.getAnonymous()) {
            return "익명";
        }
        if (post.getUser() != null) {
            UserProfile userProfile = userService.getUserProfile(post.getUser().getId());
            if (userProfile != null && userProfile.getNickname() != null) {
                return userProfile.getNickname();
            }
        }
        return "알 수 없음";
    }

    // ✅ 프로필 이미지 가져오는 함수
    default String getProfileImage(Post post, UserService userService) {
        if (post.getAnonymous()) {
            return "image/default-profile.png";
        }
        if (post.getUser() != null) {
            UserProfile userProfile = userService.getUserProfile(post.getUser().getId());
            if (userProfile != null && userProfile.getProfileImage() != null) {
                return userProfile.getProfileImage();
            }
        }
        return "image/default-profile.png";
    }

    @AfterMapping
    default void setIsBlocked(
            @MappingTarget PostListResponseDto.PostResponseDto.PostResponseDtoBuilder dtoBuilder,
            Post post,
            @Context List<String> blockedUserIdList) {

        boolean isBlocked = post.getUser() != null && blockedUserIdList.contains(post.getUser().getId());
        dtoBuilder.isBlocked(isBlocked);
    }

    // ✅ 차단된 사용자 여부 확인 함수
    default boolean isBlockedUser(Post post, List<String> blockedUserIdList) {
        return post.getUser() != null && blockedUserIdList.contains(post.getUser().getId());
    }

    // ✅ 빌더 패턴 적용: 댓글 리스트 매핑 추가 (부모 댓글만 반환)
    default List<CommentListResponseDto.CommentResponseDto> mapCommentList(
            List<Comment> comments,
            List<String> blockedUserIdList,
            String loggedInUserId,
            UserService userService,
            @Context CommentService commentService) {
        if (comments == null) return List.of();
        CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

        return comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> {
                    boolean isLiked = commentService.isUserLikedComment(loggedInUserId, comment.getId());
                    boolean isCommentAuthor = commentService.isCommentAuthor(loggedInUserId, comment.getId());

                    return commentMapper.commentToCommentResponseDto(
                            comment, blockedUserIdList, isLiked, isCommentAuthor, loggedInUserId, userService, commentService
                    );
                })
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void setIsBlockedPost(@MappingTarget PostListResponseDto.PostResponseDto.PostResponseDtoBuilder dto, Post post, @Context List<String> blockedUserIdList) {
        boolean isBlocked = blockedUserIdList.contains(post.getUser().getId());
        dto.isBlocked(isBlocked);
    }


    default long calculateCommentCount(List<Comment> comments) {
        if (comments == null) return 0;
        return comments.stream()
                .mapToLong(comment -> 1 + comment.getChildComments().size())
                .sum();
    }

    @Mapping(source = "post.category", target = "category")
    @Mapping(source = "post.user.id", target = "userId")
    @Mapping(target = "isBlocked", expression = "java(isBlockedUser(post, blockedUserIdList))")
    PostRankingResponseDto toPostRankingResponseDto(Post post, List<String> blockedUserIdList);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "likeCount", target = "likeCount")
    @Mapping(source = "commentCount", target = "commentCount")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "createdAt", target = "createdAt")
    PostSearchResponseDto.PostSearchListResponseDto toPostSearchListResponseDto(Post post);

    PostReportResponseDto toReportedPostResponseDto(ReportPostData reportedPost);
}
