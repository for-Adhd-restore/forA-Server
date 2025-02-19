package com.project.foradhd.domain.board.web.mapper;

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

    @Mapping(target = "comments", expression = "java(mapCommentList(post.getComments(), blockedUserIdList))")
    @Mapping(target = "commentCount", expression = "java(calculateCommentCount(post.getComments()))")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "nickname", target = "nickname")
    @Mapping(source = "profileImage", target = "profileImage")
    PostListResponseDto.PostResponseDto toPostResponseDto(Post post, @Context UserService userService, @Context List<String> blockedUserIdList);

    @AfterMapping
    default void setUsers(@MappingTarget Post.PostBuilder postBuilder, @Context String userId, @Context UserService userService) {
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

    @AfterMapping
    default void setAnonymousOrUserProfile(@MappingTarget PostListResponseDto.PostResponseDto.PostResponseDtoBuilder dto, Post post, @Context UserService userService) {
        if (post.getAnonymous()) {
            dto.nickname("익명");
            dto.profileImage("image/default-profile.png");
        } else if (post.getUser() != null) {
            UserProfile userProfile = userService.getUserProfile(post.getUser().getId());
            if (userProfile != null) {
                dto.nickname(userProfile.getNickname());
                dto.profileImage(userProfile.getProfileImage());
            }
        }
    }

    @AfterMapping
    default void setIsBlockedPost(@MappingTarget PostListResponseDto.PostResponseDto.PostResponseDtoBuilder dto, Post post, @Context List<String> blockedUserIdList) {
        boolean isBlocked = blockedUserIdList.contains(post.getUser().getId());
        dto.isBlocked(isBlocked);
    }

    default List<CommentListResponseDto.CommentResponseDto> mapCommentList(List<Comment> comments, List<String> blockedUserIdList) {
        if (comments == null) return List.of();
        CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

        // 부모 댓글만 매핑
        return comments.stream()
                .filter(comment -> comment.getParentComment() == null) // 부모 댓글만 필터링
                .map(comment -> commentMapper.commentToCommentResponseDto(comment, blockedUserIdList))
                .collect(Collectors.toList());
    }


    default long calculateCommentCount(List<Comment> comments) {
        if (comments == null) return 0;
        return comments.stream()
                .mapToLong(comment -> 1 + comment.getChildComments().size())
                .sum();
    }

    @Mapping(source = "category", target = "category")
    @Mapping(source = "user.id", target = "userId")
    PostRankingResponseDto toPostRankingResponseDto(Post post);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "likeCount", target = "likeCount")
    @Mapping(source = "commentCount", target = "commentCount")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "createdAt", target = "createdAt")
    PostSearchResponseDto.PostSearchListResponseDto toPostSearchListResponseDto(Post post);

    PostReportResponseDto toReportedPostResponseDto(ReportPostData reportedPost);
}
