package com.ssafy.enjoytrip.core.domain.query;

public record AttractionSearchCondition(
        Integer sidoCode,
        Integer gugunCode,
        String contentTypeId,
        String keyword,
        Double longitude,
        Double latitude,
        Double radiusMeters
) {
    public boolean aroundSearch() {
        return longitude != null && latitude != null;
    }
}
