package com.ssafy.enjoytrip.domain;

import java.time.LocalDateTime;

public record Friendship(
        Long id,
        String requesterUserId,
        String requesterDisplayName,
        String addresseeUserId,
        String addresseeDisplayName,
        FriendshipStatus status,
        LocalDateTime requestedAt,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public String counterpartUserId(String actorUserId) {
        if (requesterUserId.equals(actorUserId)) {
            return addresseeUserId;
        }
        if (addresseeUserId.equals(actorUserId)) {
            return requesterUserId;
        }
        return null;
    }

    public String counterpartDisplayName(String actorUserId) {
        if (requesterUserId.equals(actorUserId)) {
            return addresseeDisplayName;
        }
        if (addresseeUserId.equals(actorUserId)) {
            return requesterDisplayName;
        }
        return null;
    }

    public boolean isParticipant(String actorUserId) {
        return requesterUserId.equals(actorUserId) || addresseeUserId.equals(actorUserId);
    }
}
