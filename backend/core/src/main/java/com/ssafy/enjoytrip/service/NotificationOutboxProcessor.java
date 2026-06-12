package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.NOTIFICATION_OUTBOX_NOT_FOUND;

import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.repository.NotificationOutboxRepository;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxProcessor {
    private final NotificationOutboxRepository outboxRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void processOutboxEvent(Long outboxEventId) {
        NotificationOutboxEvent event = outboxRepository.findById(outboxEventId)
                .orElseThrow(() -> new CoreException(NOTIFICATION_OUTBOX_NOT_FOUND));
        if (event.isProcessed()) {
            return;
        }
        if (!event.isPending()) {
            return;
        }

        if (!notificationRepository.existsByOutboxEventId(outboxEventId)) {
            notificationRepository.saveFromOutbox(event);
        }
        outboxRepository.markProcessed(outboxEventId);
    }

    @Transactional
    public void markFailed(Long outboxEventId, String errorMessage) {
        outboxRepository.markFailed(outboxEventId, errorMessage);
    }
}
