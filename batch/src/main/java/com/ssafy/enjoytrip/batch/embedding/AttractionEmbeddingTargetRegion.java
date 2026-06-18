package com.ssafy.enjoytrip.batch.embedding;

public record AttractionEmbeddingTargetRegion(
        String sidoName,
        String gugunName,
        int sidoCode,
        int gugunCode,
        String provenance
) {
}
