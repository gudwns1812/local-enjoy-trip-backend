package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    Optional<Friendship> findById(Long id);

    boolean existsActiveBetween(String userId, String otherUserId);

    Friendship savePending(String requesterUserId, String addresseeUserId);

    Friendship updateStatus(Long id, FriendshipStatus status);

    List<Friendship> findAcceptedByUser(String userId);

    List<Friendship> findPendingReceivedByUser(String userId);

    List<Friendship> findPendingSentByUser(String userId);
}
