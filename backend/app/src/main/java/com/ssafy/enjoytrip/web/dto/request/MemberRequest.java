package com.ssafy.enjoytrip.web.dto.request;

public record MemberRequest(
    String action,
    String userId,
    String name,
    String nickname,
    String email,
    String password,
    String profileImageUrl,
    Double representativeLatitude,
    Double representativeLongitude,
    String representativeRegionName,
    String oauthSignupTicket
) {}
