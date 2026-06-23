package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.domain.FriendshipStatus;
import com.ssafy.enjoytrip.core.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.core.domain.NotificationType;
import com.ssafy.enjoytrip.storage.db.core.model.FriendshipRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NotificationRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.FriendshipMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationFriendshipMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @DisplayName("FriendshipMapper는 H2 인메모리 DB에서 요청 조회와 상태 전이를 수행한다")
    @Test
    void friendshipMapperPersistsAndTransitionsFriendship() {
        Long requesterMemberId = seedMember("requester", uniqueId("requester") + "@example.com");
        Long addresseeMemberId = seedMember("addressee", uniqueId("addressee") + "@example.com");
        FriendshipRecord record = new FriendshipRecord(requesterMemberId, addresseeMemberId);

        friendshipMapper.insert(record);
        record.transitionTo(FriendshipStatus.ACCEPTED);
        friendshipMapper.updateStatus(record);

        FriendshipRecord saved = friendshipMapper.findById(record.getId());

        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        assertThat(saved.getRespondedAt()).isNotNull();
        assertThat(friendshipMapper.findByParticipantAndStatus(requesterMemberId, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.findReceivedRequests(addresseeMemberId, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.findSentRequests(requesterMemberId, FriendshipStatus.ACCEPTED))
                .extracting(FriendshipRecord::getId)
                .contains(record.getId());
        assertThat(friendshipMapper.existsActiveBetween(
                requesterMemberId,
                addresseeMemberId,
                List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED)
        )).isEqualTo(1);
    }

    @DisplayName("NotificationMapper는 친구 요청 알림을 reference 기준으로 읽음 처리한다")
    @Test
    void notificationMapperFindsAndMarksFriendRequestNotifications() {
        Long requesterMemberId = seedMember("noti-requester", uniqueId("noti-requester") + "@example.com");
        Long recipientMemberId = seedMember("noti-recipient", uniqueId("noti-recipient") + "@example.com");
        FriendshipRecord friendship = new FriendshipRecord(requesterMemberId, recipientMemberId);
        friendshipMapper.insert(friendship);
        jdbcTemplate.update("""
                insert into notifications (
                    recipient_member_id,
                    type,
                    reference_type,
                    reference_id,
                    payload,
                    created_at
                ) values (?, ?, ?, ?, ?, current_timestamp)
                """,
                recipientMemberId,
                NotificationType.FRIEND_REQUEST_RECEIVED.name(),
                NotificationReferenceType.FRIENDSHIP.name(),
                friendship.getId(),
                String.valueOf(requesterMemberId)
        );
        notificationMapper.markReadByReference(
                recipientMemberId,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId(),
                LocalDateTime.now()
        );
        NotificationRecord saved = notificationMapper.findByBusinessKey(
                recipientMemberId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                friendship.getId()
        );

        assertThat(notificationMapper.existsUnreadFriendRequest(
                recipientMemberId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                FriendshipStatus.PENDING
        )).isZero();
        assertThat(notificationMapper.findUnreadFriendRequests(
                recipientMemberId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                FriendshipStatus.PENDING,
                10
        )).isEmpty();
        assertThat(saved.getReadAt()).isNotNull();
    }
}
