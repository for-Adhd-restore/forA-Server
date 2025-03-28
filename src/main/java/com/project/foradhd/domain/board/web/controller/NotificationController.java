package com.project.foradhd.domain.board.web.controller;

import com.project.foradhd.domain.board.business.service.NotificationService;
import com.project.foradhd.global.AuthUserId;
import com.project.foradhd.global.util.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitters sseEmitters;

    @GetMapping("/sse")
    public SseEmitter streamSseMvc(@AuthUserId String userId) {
        // 타임아웃을 1시간(3600000ms)으로 설정
        SseEmitter emitter = new SseEmitter(3600000L);
        sseEmitters.addEmitter(userId, emitter);

        // 첫 연결 시 더미 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("dummy eventName")
                    .data("dummy data")
                    .reconnectTime(3000L));
        } catch (Exception e) {
            sseEmitters.removeEmitter(userId, emitter);
        }

        // SSE 연결의 상태를 확인하기 위해 각 이벤트 처리 콜백 설정
        emitter.onCompletion(() -> sseEmitters.removeEmitter(userId, emitter));
        emitter.onTimeout(() -> sseEmitters.removeEmitter(userId, emitter));
        emitter.onError((e) -> sseEmitters.removeEmitter(userId, emitter));

        return emitter;
    }

    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> sendTestNotification(@AuthUserId String userId) {
        String message = "🔔 테스트 알림입니다!";
        notificationService.createNotification(userId, message);
        sseEmitters.sendNotification(userId, message);
        return ResponseEntity.ok().build();
    }
}
