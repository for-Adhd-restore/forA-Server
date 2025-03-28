package com.project.foradhd.domain.board.business.service.Impl;

import com.project.foradhd.domain.board.business.service.NotificationService;
import com.project.foradhd.domain.board.persistence.entity.Notification;
import com.project.foradhd.domain.board.persistence.repository.NotificationRepository;
import com.project.foradhd.domain.user.business.service.UserService;
import com.project.foradhd.domain.user.persistence.entity.User;
import com.project.foradhd.domain.user.persistence.repository.UserRepository;
import com.project.foradhd.global.exception.BusinessException;
import com.project.foradhd.global.exception.ErrorCode;
import com.project.foradhd.global.util.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final SseEmitters sseEmitters;

    @Override
    public void createNotification(String userId, String message) {
        User user = userService.getUser(userId);
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        sseEmitters.sendNotification(userId, message);
    }

    @Override
    public List<Notification> getNotifications(String userId) {
        User user = userService.getUser(userId);

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTIFICATION));
        notification.setRead(true);
    }
}
