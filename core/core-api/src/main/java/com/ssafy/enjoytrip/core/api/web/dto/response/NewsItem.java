package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.NewsResult;
public record NewsItem(
        String id,
        String title,
        String link,
        String summary,
        String source,
        String publishedAt
) {
    public static NewsItem from(NewsResult item) {
        return new NewsItem(
                item.id(),
                item.title(),
                item.link(),
                item.summary(),
                item.source(),
                item.publishedAt()
        );
    }
}
