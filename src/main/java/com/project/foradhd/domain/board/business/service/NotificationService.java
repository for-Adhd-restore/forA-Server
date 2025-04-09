package com.project.foradhd.domain.board.business.service;

import com.project.foradhd.domain.board.persistence.entity.Notification;
import com.project.foradhd.domain.board.persistence.entity.Post;

import java.util.List;

public interface NotificationService {
    void createNotification(String userId, String message, Post post);
    List<Notification> getNotifications(String userId);
    void markAsRead(Long notificationId);
}
