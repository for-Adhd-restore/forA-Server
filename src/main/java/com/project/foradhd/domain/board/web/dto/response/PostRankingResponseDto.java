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
@NoArgsConstructor
@AllArgsConstructor
public class PostRankingResponseDto {
    private Long id;
    private String userId;
    private String title;
    private Category category;
    private long viewCount;
    private long likeCount;

    @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
    private LocalDateTime createdAt;
    private List<String> images;
    private Boolean isBlocked;



    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PagedPostRankingResponseDto {
        private String category;
        private PagingResponse paging;
        private List<PostRankingResponseDto> postList;
    }
}
