package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.service.NearbyAttractionCandidate;
import com.ssafy.enjoytrip.core.domain.MapExploreFilter;
import com.ssafy.enjoytrip.core.domain.NoteCategory;
import com.ssafy.enjoytrip.core.domain.query.MapNotesCondition;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapCenterResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.MapExploreResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteMapPinResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.PlaceMapPinResponse;
import com.ssafy.enjoytrip.core.domain.query.NearbySearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapExploreService {
    private final AttractionService attractionService;
    private final NoteService noteService;

    public MapExploreResponse explore(
            String viewerUserId,
            double longitude,
            double latitude,
            double radiusMeters,
            int limit,
            MapExploreFilter filter,
            NoteCategory noteCategory
    ) {
        MapCenterResponse center = new MapCenterResponse(longitude, latitude, null);
        List<PlaceMapPinResponse> places = findPlacePins(viewerUserId, radiusMeters, limit, filter, center);
        List<NoteMapPinResponse> notes = findNotePins(
                viewerUserId,
                radiusMeters,
                limit,
                filter,
                noteCategory,
                center
        );

        return new MapExploreResponse(
                center,
                radiusMeters,
                limit,
                filter,
                places,
                notes
        );
    }

    private List<PlaceMapPinResponse> findPlacePins(
            String viewerUserId,
            double radiusMeters,
            int limit,
            MapExploreFilter filter,
            MapCenterResponse center
    ) {
        if (!filter.includesPlaces()) {
            return List.of();
        }

        return attractionService.findNearbyCandidates(
                        new NearbySearchCondition(
                                center.longitude(),
                                center.latitude(),
                                radiusMeters,
                                limit
                        ),
                        viewerUserId,
                        filter.savedPlacesOnly()
                )
                .stream()
                .map(MapExploreService::toPlacePin)
                .toList();
    }

    private static PlaceMapPinResponse toPlacePin(NearbyAttractionCandidate candidate) {
        Attraction attraction = candidate.attraction();

        return new PlaceMapPinResponse(
                attraction.id(),
                attraction.title(),
                attraction.addr1(),
                attraction.latitude(),
                attraction.longitude(),
                attraction.primaryImageUrl(),
                attraction.contentTypeId(),
                candidate.distanceMeters(),
                attraction.favorited(),
                attraction.saved(),
                attraction.saveCount(),
                attraction.ratingAverage(),
                attraction.ratingCount()
        );
    }

    private List<NoteMapPinResponse> findNotePins(
            String viewerUserId,
            double radiusMeters,
            int limit,
            MapExploreFilter filter,
            NoteCategory noteCategory,
            MapCenterResponse center
    ) {
        if (!filter.includesNotes()) {
            return List.of();
        }

        return noteService.findMapNotes(new MapNotesCondition(
                center.longitude(),
                center.latitude(),
                radiusMeters,
                limit,
                viewerUserId,
                noteCategory,
                filter.friendNotesOnly()
        ));
    }
}
