package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import java.util.List;

public record MapExploreResponse(
        MapCenterResponse center,
        double radiusMeters,
        int limit,
        MapExploreFilter filter,
        List<PlaceMapPinResponse> places,
        List<NoteMapPinResponse> notes
) {
    public MapExploreResponse {
        places = List.copyOf(places);
        notes = List.copyOf(notes);
    }
}
