package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.AttractionStats;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionAverageRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AttractionMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttractionStatsReader {
    private final AttractionMapper attractionMapper;

    public AttractionStats findStats(Long attractionId, String userId) {
        return findStatsByAttractionId(List.of(attractionId), userId)
                .getOrDefault(attractionId, new AttractionStats(
                        attractionId,
                        0,
                        0.0,
                        0,
                        List.of(),
                        false,
                        null
                ));
    }

    public Map<Long, AttractionStats> findStatsByAttractionId(List<Long> attractionIds, String userId) {
        if (attractionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> favoriteCounts = findFavoriteCounts(attractionIds);
        Map<Long, AttractionAverageRatingRecord> ratingStats = findRatingStats(attractionIds);
        Set<Long> favoritedIds = findFavoritedIds(attractionIds, userId);
        Map<Long, Integer> myRatings = findMyRatings(attractionIds, userId);

        Map<Long, AttractionStats> result = new HashMap<>();
        for (Long attractionId : attractionIds) {
            AttractionAverageRatingRecord rating = ratingStats.get(attractionId);
            result.put(attractionId, new AttractionStats(
                    attractionId,
                    favoriteCounts.getOrDefault(attractionId, 0),
                    rating == null ? 0.0 : rating.average(),
                    rating == null ? 0 : rating.count(),
                    findTags(attractionId),
                    favoritedIds.contains(attractionId),
                    myRatings.get(attractionId)
            ));
        }

        return result;
    }

    private Map<Long, Integer> findFavoriteCounts(List<Long> attractionIds) {
        return attractionMapper.findFavoriteCounts(attractionIds).stream()
                .collect(Collectors.toMap(AttractionCountRecord::attractionId, AttractionCountRecord::count));
    }

    private Map<Long, AttractionAverageRatingRecord> findRatingStats(List<Long> attractionIds) {
        return attractionMapper.findRatingStats(attractionIds).stream()
                .collect(Collectors.toMap(AttractionAverageRatingRecord::attractionId, record -> record));
    }

    private Set<Long> findFavoritedIds(List<Long> attractionIds, String userId) {
        if (userId == null) {
            return Set.of();
        }

        return new HashSet<>(attractionMapper.findFavoritedIds(attractionIds, userId));
    }

    private Map<Long, Integer> findMyRatings(List<Long> attractionIds, String userId) {
        if (userId == null) {
            return Map.of();
        }

        return attractionMapper.findMyRatings(attractionIds, userId).stream()
                .collect(Collectors.toMap(
                        AttractionRatingRecord::attractionId,
                        AttractionRatingRecord::rating
                ));
    }

    private List<AttractionTag> findTags(Long attractionId) {
        return attractionMapper.findTagsByAttractionId(attractionId).stream()
                .map(record -> new AttractionTag(record.id(), record.name()))
                .toList();
    }
}
