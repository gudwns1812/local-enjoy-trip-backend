package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.NotificationEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByOutboxEventId(Long outboxEventId);

    Optional<NotificationEntity> findByOutboxEventId(Long outboxEventId);

    List<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDescIdDesc(String recipientUserId, Pageable pageable);

    List<NotificationEntity> findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDescIdDesc(String recipientUserId,
                                                                                             Pageable pageable);

    Optional<NotificationEntity> findByIdAndRecipientUserId(Long id, String recipientUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationEntity n
            set n.readAt = :readAt
            where n.recipientUserId = :recipientUserId
              and n.readAt is null
            """)
    int markAllRead(@Param("recipientUserId") String recipientUserId, @Param("readAt") LocalDateTime readAt);
}
