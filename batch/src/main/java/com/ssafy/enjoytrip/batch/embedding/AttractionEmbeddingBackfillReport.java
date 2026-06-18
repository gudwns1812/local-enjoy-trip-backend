package com.ssafy.enjoytrip.batch.embedding;

public record AttractionEmbeddingBackfillReport(
        int selectedCount,
        int embeddedCount,
        int skippedCount,
        int failedCount,
        boolean dryRun
) {
}
