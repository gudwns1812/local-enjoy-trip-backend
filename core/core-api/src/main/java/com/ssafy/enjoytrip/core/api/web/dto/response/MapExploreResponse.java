package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.service.MapExploreResult;
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

    public static MapExploreResponse from(MapExploreResult result) {
        return new MapExploreResponse(
                MapCenterResponse.from(result.center()),
                result.radiusMeters(),
                result.limit(),
                result.filter(),
                result.places().stream().map(PlaceMapPinResponse::from).toList(),
                result.notes().stream().map(NoteMapPinResponse::from).toList()
        );
    }
}
