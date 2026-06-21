package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.AttractionStats;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionStatsService {
    private final AttractionStatsReader attractionStatsReader;

    public AttractionStats findStats(Long attractionId, String userId) {
        return attractionStatsReader.findStats(attractionId, userId);
    }

    public Map<Long, AttractionStats> findStatsByAttractionId(List<Long> attractionIds, String userId) {
        return attractionStatsReader.findStatsByAttractionId(attractionIds, userId);
    }
}
