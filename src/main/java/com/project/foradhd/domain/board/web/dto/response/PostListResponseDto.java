package com.project.foradhd.domain.board.web.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.global.paging.web.dto.response.PagingResponse;
import com.project.foradhd.global.serializer.LocalDateTimeToEpochSecondSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostListResponseDto {

    private List<PostResponseDto> postList;
    private PagingResponse paging;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostResponseDto {
        private Long id;
        private String userId;
        private String title;
        private String content;
        private boolean anonymous;
        private List<String> images;
        private long likeCount;
        private long commentCount;
        private long scrapCount;
        private long viewCount;
        private Category category;
        private List<CommentListResponseDto.CommentResponseDto> comments;
        private String nickname;
        private String profileImage;
        private Boolean isBlocked;
        private Boolean isAuthor;
        private Boolean isScrapped;
        private Boolean isLiked;

        @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
        private LocalDateTime createdAt;
        @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
        private LocalDateTime lastModifiedAt;
    }
}
