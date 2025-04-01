package com.project.foradhd.domain.board.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String content;
    private boolean isRead;
    private String notificationType;
    private String createdAt;

    private UserProfileInfo userProfile;

    @Getter
    @Builder
    public static class UserProfileInfo {
        private String nickname;
        private String profileImageUrl;
    }
}
