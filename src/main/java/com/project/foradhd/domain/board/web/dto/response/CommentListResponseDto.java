package com.project.foradhd.domain.board.web.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import com.project.foradhd.global.serializer.LocalDateTimeToEpochSecondSerializer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder(toBuilder = true)
public class CommentListResponseDto {

    private List<CommentResponseDto> commentList;
    private PagingResponse paging;

    @Getter
    @Builder
    public static class CommentResponseDto {
        private final Long id;
        private final String content;
        private final String userId;
        private final Long postId;
        private final boolean anonymous;
        private final long likeCount;

        @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
        private LocalDateTime createdAt;
        @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
        private LocalDateTime lastModifiedAt;

        private final Long parentCommentId;
        private final List<CommentResponseDto> children;
        private final String nickname;
        private final String profileImage;
        private Boolean isBlocked;
        private final Boolean isCommentAuthor;
    }
}
