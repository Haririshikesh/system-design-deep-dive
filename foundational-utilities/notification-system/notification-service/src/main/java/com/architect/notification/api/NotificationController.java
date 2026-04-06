package com.architect.notification.api;

import com.architect.notification.model.NotificationRequest;
import com.architect.notification.model.NotificationResponse;
import com.architect.notification.service.NotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationFacade notificationFacade;

    @PostMapping
    public ResponseEntity<NotificationResponse> dispatchNotification(@RequestBody NotificationRequest request) {
        NotificationResponse response = notificationFacade.dispatch(request);
        return ResponseEntity.accepted().body(response);
    }
}
