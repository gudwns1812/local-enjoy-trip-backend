package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationReadAllResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationResponse;
import com.ssafy.enjoytrip.web.dto.response.NotificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "Notifications", description = "동네핀 저장형 인앱 알림 API")
public interface NotificationApi {
    @Operation(summary = "내 알림 조회", operationId = "getNotifications")
    ApiResponse<NotificationsResponse> notifications(boolean unreadOnly, Integer limit, Jwt jwt);

    @Operation(summary = "알림 읽음 처리", operationId = "markNotificationRead")
    ApiResponse<NotificationResponse> markRead(Long notificationId, Jwt jwt);

    @Operation(summary = "내 알림 전체 읽음 처리", operationId = "markAllNotificationsRead")
    ApiResponse<NotificationReadAllResponse> markAllRead(Jwt jwt);
}
