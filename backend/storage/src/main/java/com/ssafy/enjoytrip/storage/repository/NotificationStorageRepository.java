package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationOutboxEvent;
import com.ssafy.enjoytrip.repository.NotificationRepository;
import com.ssafy.enjoytrip.storage.entity.NotificationEntity;
import com.ssafy.enjoytrip.storage.jpa.NotificationJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NotificationStorageRepository implements NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public boolean existsByOutboxEventId(Long outboxEventId) {
        return notificationJpaRepository.existsByOutboxEventId(outboxEventId);
    }

    @Override
    @Transactional
    public Notification saveFromOutbox(NotificationOutboxEvent event) {
        try {
            NotificationEntity entity = new NotificationEntity(
                    event.recipientUserId(),
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.payload(),
                    event.id()
            );
            return toModel(notificationJpaRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException duplicate) {
            return notificationJpaRepository.findByOutboxEventId(event.id())
                    .map(NotificationStorageRepository::toModel)
                    .orElseThrow(() -> duplicate);
        }
    }

    @Override
    public List<Notification> findByRecipient(String recipientUserId, boolean unreadOnly, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<NotificationEntity> entities = unreadOnly
                ? notificationJpaRepository.findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDescIdDesc(recipientUserId, page)
                : notificationJpaRepository.findByRecipientUserIdOrderByCreatedAtDescIdDesc(recipientUserId, page);
        return entities.stream().map(NotificationStorageRepository::toModel).toList();
    }

    @Override
    @Transactional
    public Optional<Notification> markRead(Long notificationId, String recipientUserId) {
        return notificationJpaRepository.findByIdAndRecipientUserId(notificationId, recipientUserId)
                .map(entity -> {
                    entity.markRead();
                    return toModel(entity);
                });
    }

    @Override
    @Transactional
    public int markAllRead(String recipientUserId) {
        return notificationJpaRepository.markAllRead(recipientUserId, LocalDateTime.now());
    }

    private static Notification toModel(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getType(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getPayload(),
                entity.getOutboxEventId(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
