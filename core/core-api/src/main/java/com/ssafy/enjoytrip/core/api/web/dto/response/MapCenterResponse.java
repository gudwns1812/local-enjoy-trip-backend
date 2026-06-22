package com.ssafy.enjoytrip.core.api.web.dto.response;

public record MapCenterResponse(
        double longitude,
        double latitude,
        String regionName
) {
}
