package com.ssafy.enjoytrip.core.domain.event;

public record FriendshipRequestedEvent(
        Long friendshipId,
        String requesterUserId,
        String recipientUserId
) {
}
