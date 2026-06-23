package com.ssafy.enjoytrip.external.courseorder;

import java.util.List;

public record CourseOrderRecommendationRequest(
        String courseId,
        List<CourseOrderRecommendationItem> items
) {
    public CourseOrderRecommendationRequest {
        items = List.copyOf(items == null ? List.of() : items);
    }
}
