package com.ssafy.enjoytrip.batch;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AttractionEmbeddingTargetRegionValidator {
    private static final Set<String> REQUIRED_REGION_KEYS = Set.of("32:1:강원특별자치도:강릉시", "37:12:전북특별자치도:전주시");

    public void validate(List<AttractionEmbeddingTargetRegion> regions) {
        if (regions == null || regions.size() != REQUIRED_REGION_KEYS.size()) {
            throw new IllegalStateException(
                    "관광지 임베딩 대상 지역은 강릉과 전주만 포함해야 합니다."
            );
        }
        Set<String> actual = regions.stream()
                .map(region -> region.sidoCode() + ":" + region.gugunCode() + ":" + region.sidoName() + ":" + region.gugunName())
                .collect(Collectors.toSet());
        if (!actual.equals(REQUIRED_REGION_KEYS)) {
            throw new IllegalStateException(
                    "관광지 임베딩 대상 지역 코드 쌍이 "
                            + "정식 증빙 산출물과 일치하지 않습니다."
            );
        }
        boolean missingProof = regions.stream()
                .anyMatch(region -> region.provenance() == null || region.provenance().isBlank());
        if (missingProof) {
            throw new IllegalStateException(
                    "관광지 임베딩 대상 지역에는 출처 증빙이 필요합니다."
            );
        }
    }
}
