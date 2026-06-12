package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.service.NotificationService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.NotificationApi;
import com.ssafy.enjoytrip.web.dto.response.NotificationReadAllResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationsResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @GetMapping
    @Override
    public ApiResponse<NotificationsResponse> notifications(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                                            @RequestParam(required = false) Integer limit,
                                                            @AuthenticationPrincipal Jwt jwt) {
        List<Notification> notifications = notificationService.findNotifications(authenticatedUserId(jwt), unreadOnly, limit);
        return success(NotificationsResponse.from(notifications));
    }

    @PatchMapping("/{notificationId}/read")
    @Override
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long notificationId,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return success(NotificationResponse.from(
                notificationService.markRead(notificationId, authenticatedUserId(jwt))
        ));
    }

    @PatchMapping("/read-all")
    @Override
    public ApiResponse<NotificationReadAllResponse> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        return success(new NotificationReadAllResponse(
                notificationService.markAllRead(authenticatedUserId(jwt))
        ));
    }

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return jwt.getSubject().trim();
    }
}
