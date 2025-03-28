package com.project.foradhd.domain.board.web.mapper;

import com.project.foradhd.domain.board.persistence.entity.Notification;
import com.project.foradhd.domain.board.web.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mappings({
            @Mapping(source = "user.userProfile.nickname", target = "userProfile.nickname"),
            @Mapping(source = "user.userProfile.profileImageUrl", target = "userProfile.profileImageUrl"),
            @Mapping(source = "notificationType", target = "notificationType", qualifiedByName = "toStringValue"),
            @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    })
    NotificationResponse toDto(Notification notification);
}
