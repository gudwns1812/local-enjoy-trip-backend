package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttractionPopularityStatsMaintenanceService {
    private final AttractionPopularityDeltaBuffer deltaBuffer;
    private final AttractionMapper attractionMapper;

    @Transactional
    public int flushBufferedFavoriteDeltas() {
        Map<Long, Integer> deltas = deltaBuffer.drainFavoriteDeltas();
        int appliedCount = 0;
        for (Map.Entry<Long, Integer> entry : deltas.entrySet()) {
            attractionMapper.incrementPopularityFavoriteCount(entry.getKey(), entry.getValue());
            appliedCount++;
        }

        return appliedCount;
    }

    @Transactional
    public int reconcileFavoriteCounts() {
        return attractionMapper.reconcilePopularityFavoriteCounts();
    }
}
