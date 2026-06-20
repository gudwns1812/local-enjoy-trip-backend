package com.ssafy.enjoytrip.core.domain.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisAttractionPopularityDeltaCache implements AttractionPopularityDeltaCache {
    private static final String DIRTY_IDS_KEY = "enjoytrip:attraction-popularity:dirty";
    private static final String DELTA_KEY_PREFIX = "enjoytrip:attraction-popularity:delta:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void recordFavoriteDelta(Long attractionId, long delta) {
        if (attractionId == null || delta == 0) {
            return;
        }

        try {
            redisTemplate.opsForValue().increment(deltaKey(attractionId), delta);
            redisTemplate.opsForSet().add(DIRTY_IDS_KEY, attractionId.toString());
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to record attraction popularity delta. attractionId={}, delta={}",
                    attractionId,
                    delta,
                    exception
            );
        }
    }

    @Override
    public Map<Long, Long> drainDirtyDeltas(int batchSize) {
        if (batchSize <= 0) {
            return Map.of();
        }

        try {
            return drainDirtyDeltas(redisTemplate.opsForSet().pop(DIRTY_IDS_KEY, batchSize));
        } catch (RuntimeException exception) {
            log.warn("Failed to drain attraction popularity deltas", exception);
            return Map.of();
        }
    }

    private Map<Long, Long> drainDirtyDeltas(List<String> dirtyIds) {
        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> deltas = new LinkedHashMap<>();
        for (String dirtyId : dirtyIds) {
            putDrainedDelta(deltas, dirtyId);
        }
        return deltas;
    }

    private void putDrainedDelta(Map<Long, Long> deltas, String dirtyId) {
        Long attractionId = parseAttractionId(dirtyId);
        if (attractionId == null) {
            return;
        }

        Long delta = drainDelta(attractionId);
        if (delta == null || delta == 0) {
            return;
        }

        deltas.put(attractionId, delta);
    }

    private Long drainDelta(Long attractionId) {
        String key = deltaKey(attractionId);
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value == null) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            log.warn("Invalid attraction popularity delta value. key={}, value={}", key, value);
            return null;
        }
    }

    private static Long parseAttractionId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            log.warn("Invalid dirty attraction id. value={}", value);
            return null;
        }
    }

    private static String deltaKey(Long attractionId) {
        return DELTA_KEY_PREFIX + attractionId;
    }
}
