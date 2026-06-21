package com.ssafy.enjoytrip.core.domain;

import java.time.LocalDateTime;

public record Notification(
        Long id,
        String recipientUserId,
        NotificationType type,
        NotificationReferenceType referenceType,
        Long referenceId,
        String payload,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
