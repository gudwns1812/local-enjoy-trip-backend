package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.MapCenter;

public record MapCenterResponse(
        double longitude,
        double latitude,
        String regionName
) {
    public static MapCenterResponse from(MapCenter center) {
        return new MapCenterResponse(
                center.longitude(),
                center.latitude(),
                center.regionName()
        );
    }
}
