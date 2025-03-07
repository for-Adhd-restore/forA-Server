package com.project.foradhd.domain.board.business.service.dto.in;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.project.foradhd.domain.board.persistence.entity.Comment;
import com.project.foradhd.domain.board.persistence.entity.Post;
import com.project.foradhd.domain.board.persistence.enums.Category;
import com.project.foradhd.domain.board.persistence.enums.Report;
import com.project.foradhd.global.serializer.LocalDateTimeToEpochSecondSerializer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportPostData {
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
    private List<Comment> comments;
    private String nickname;
    private String profileImage;
    private String email;
    private HashMap<Report, Integer> reportTypeCounts;

    @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
    private LocalDateTime createdAt;
    @JsonSerialize(using = LocalDateTimeToEpochSecondSerializer.class)
    private LocalDateTime lastModifiedAt;
}
