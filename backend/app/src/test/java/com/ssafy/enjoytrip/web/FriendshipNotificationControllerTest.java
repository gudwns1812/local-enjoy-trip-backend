package com.ssafy.enjoytrip.web;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.enjoytrip.domain.Friendship;
import com.ssafy.enjoytrip.domain.FriendshipStatus;
import com.ssafy.enjoytrip.domain.Notification;
import com.ssafy.enjoytrip.domain.NotificationReferenceType;
import com.ssafy.enjoytrip.domain.NotificationType;
import com.ssafy.enjoytrip.service.FriendshipService;
import com.ssafy.enjoytrip.service.NotificationService;
import com.ssafy.enjoytrip.web.controller.FriendshipController;
import com.ssafy.enjoytrip.web.controller.NotificationController;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FriendshipNotificationControllerTest {
    private FriendshipService friendshipService;
    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        friendshipService = mock(FriendshipService.class);
        notificationService = mock(NotificationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new FriendshipController(friendshipService),
                        new NotificationController(notificationService)
                )
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @DisplayName("인증 사용자는 JSON 본문으로 친구 요청을 생성한다")
    @Test
    void requestFriendshipWithJsonBody() throws Exception {
        when(friendshipService.requestFriendship(eq("alice"), eq("bob")))
                .thenReturn(friendship(1L, "alice", "bob", FriendshipStatus.PENDING));

        mockMvc.perform(post("/api/friendships/requests")
                        .principal(jwtPrincipal("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":\" bob \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.friendship.id").value(1))
                .andExpect(jsonPath("$.data.friendship.status").value("PENDING"))
                .andExpect(jsonPath("$.error", nullValue()));

        verify(friendshipService).requestFriendship("alice", "bob");
    }

    @DisplayName("인증 없이 친구 요청을 보내면 표준 인증 오류 envelope를 반환한다")
    @Test
    void requestFriendshipRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/friendships/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\":\"bob\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @DisplayName("친구 목록은 현재 사용자 관점의 counterpart를 friends 필드로 반환한다")
    @Test
    void friendsReturnsCounterpartList() throws Exception {
        when(friendshipService.findFriends("alice"))
                .thenReturn(List.of(friendship(1L, "bob", "alice", FriendshipStatus.ACCEPTED)));

        mockMvc.perform(get("/api/friendships").principal(jwtPrincipal("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.friends[0].friendshipId").value(1))
                .andExpect(jsonPath("$.data.friends[0].userId").value("bob"));
    }

    @DisplayName("내 알림 조회는 notifications 필드와 읽음 상태를 반환한다")
    @Test
    void notificationsReturnsCurrentRecipientNotifications() throws Exception {
        when(notificationService.findNotifications("bob", true, 10))
                .thenReturn(List.of(notification(3L, "bob", null)));

        mockMvc.perform(get("/api/notifications?unreadOnly=true&limit=10")
                        .principal(jwtPrincipal("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notifications[0].id").value(3))
                .andExpect(jsonPath("$.data.notifications[0].type").value("FRIEND_REQUEST_RECEIVED"))
                .andExpect(jsonPath("$.data.notifications[0].read").value(false));

        verify(notificationService).findNotifications("bob", true, 10);
    }

    @DisplayName("알림 읽음 처리는 인증 사용자 id와 notification id를 service로 전달한다")
    @Test
    void markNotificationReadDelegatesCurrentRecipient() throws Exception {
        LocalDateTime readAt = LocalDateTime.of(2026, 6, 12, 11, 0);
        when(notificationService.markRead(3L, "bob"))
                .thenReturn(notification(3L, "bob", readAt));

        mockMvc.perform(patch("/api/notifications/3/read").principal(jwtPrincipal("bob")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.read").value(true));

        verify(notificationService).markRead(3L, "bob");
    }

    private static Friendship friendship(Long id,
                                         String requesterUserId,
                                         String addresseeUserId,
                                         FriendshipStatus status) {
        return new Friendship(
                id,
                requesterUserId,
                requesterUserId,
                addresseeUserId,
                addresseeUserId,
                status,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null
        );
    }

    private static Notification notification(Long id, String recipientUserId, LocalDateTime readAt) {
        return new Notification(
                id,
                recipientUserId,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                NotificationReferenceType.FRIENDSHIP,
                1L,
                "{\"requesterUserId\":\"alice\"}",
                10L,
                readAt,
                LocalDateTime.of(2026, 6, 12, 10, 0),
                null
        );
    }

    private static Principal jwtPrincipal(String subject) {
        Jwt jwt = Jwt.withTokenValue("token-" + subject)
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.parse("2026-06-12T00:00:00Z"))
                .expiresAt(Instant.parse("2026-06-12T02:00:00Z"))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
