package com.ssafy.enjoytrip.core.api.web.dto.response;

public record PlaceMapPinResponse(
        Long id,
        String title,
        String address,
        Double latitude,
        Double longitude,
        String imageUrl,
        String contentTypeId,
        double distanceMeters,
        boolean favorited,
        boolean saved,
        int saveCount,
        double ratingAverage,
        int ratingCount
) {
}
