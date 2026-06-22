package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.NewsResult;
import java.util.List;

public record NewsResponse(List<NewsItem> news) {
    public static NewsResponse from(List<NewsResult> news) {
        return new NewsResponse(news.stream()
                .map(NewsItem::from)
                .toList());
    }
}
