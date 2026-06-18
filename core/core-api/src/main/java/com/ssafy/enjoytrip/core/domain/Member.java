package com.ssafy.enjoytrip.core.domain;

public record Member(
        String userId,
        String name,
        String nickname,
        String email,
        String password,
        String profileImageUrl,
        Double representativeLatitude,
        Double representativeLongitude,
        String representativeRegionName,
        String createdAt
) {
    public Member(String userId, String name, String email, String password, String createdAt) {
        this(userId, name, name, email, password, null, null, null, null, createdAt);
    }

    public Member withPassword(String newPassword) {
        return new Member(
                userId,
                name,
                nickname,
                email,
                newPassword,
                profileImageUrl,
                representativeLatitude,
                representativeLongitude,
                representativeRegionName,
                createdAt
        );
    }

    public String displayName() {
        if (!isBlank(nickname)) {
            return nickname;
        }
        if (!isBlank(name)) {
            return name;
        }
        return userId;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
