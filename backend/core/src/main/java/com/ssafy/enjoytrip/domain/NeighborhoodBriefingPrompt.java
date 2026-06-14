package com.ssafy.enjoytrip.domain;

import java.util.List;

public record NeighborhoodBriefingPrompt(
        String region,
        String season,
        WeatherSummary weather,
        List<CourseBriefingCandidate> courseCandidates
) {
    public NeighborhoodBriefingPrompt {
        courseCandidates = courseCandidates == null ? List.of() : List.copyOf(courseCandidates);
    }
}
