package com.ssafy.enjoytrip.external.courseorder;

public record CourseOrderRecommendationItem(
        Long id,
        String itemType,
        Long targetId,
        String title,
        int day,
        int currentPosition,
        Double latitude,
        Double longitude
) {
}
