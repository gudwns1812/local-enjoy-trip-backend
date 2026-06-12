package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.NotificationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutboxEntity, Long> {
}
