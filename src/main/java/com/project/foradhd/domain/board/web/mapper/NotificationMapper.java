package com.project.foradhd.domain.board.web.mapper;

import com.project.foradhd.domain.board.persistence.entity.Notification;
import com.project.foradhd.domain.board.web.dto.response.NotificationResponse;
import com.project.foradhd.domain.user.persistence.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mappings({
//            @Mapping(source = "user.userProfile.nickname", target = "userProfile.nickname"),
//            @Mapping(source = "user.userProfile.profileImage", target = "userProfile.profileImageUrl"),
            @Mapping(source = "createdAt", target = "createdAt"),
            @Mapping(source = "read", target = "isRead"),
            @Mapping(target = "userProfile", expression = "java(toUserProfile(notification.getUser().getUserProfile()))"),
            @Mapping(target = "notificationType", constant = "NOTIFICATION"),
            @Mapping(target = "content", source = "message"),
            @Mapping(target = "postId", expression = "java(notification.getPost() != null ? notification.getPost().getId() : null)")
    })
    NotificationResponse toDto(Notification notification);

    default NotificationResponse.UserProfileInfo toUserProfile(UserProfile profile) {
        return NotificationResponse.UserProfileInfo.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImage())
                .build();
    }
}
