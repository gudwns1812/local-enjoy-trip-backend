package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    boolean existsByOutboxEventId(Long outboxEventId);

    Notification saveFromOutbox(NotificationOutboxEvent event);

    List<Notification> findByRecipient(String recipientUserId, boolean unreadOnly, int limit);

    Optional<Notification> markRead(Long notificationId, String recipientUserId);

    int markAllRead(String recipientUserId);
}
