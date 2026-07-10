package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import java.util.List;

public record Course(
        String id,
        Long ownerMemberId,
        CourseInfo info,
        Coordinate startLocation,
        Double distanceMeters,
        int saveCount,
        String createdAt,
        String updatedAt,
        List<CourseStop> stops,
        List<Tag> tags
) {
    public Course {
        stops = List.copyOf(stops == null ? List.of() : stops);
        tags = List.copyOf(tags == null ? List.of() : tags);
    }

    public void requireOwnedBy(Long memberId) {
        if (!ownerMemberId.equals(memberId)) {
            throw new CoreException(ErrorType.COURSE_ACCESS_DENIED);
        }
    }

    public String title() {
        return info.title();
    }

    public String regionName() {
        return info.regionName();
    }

    public String date() {
        return info.date();
    }

    public List<CourseStop> items() {
        return stops;
    }

    public Course withStops(List<CourseStop> nextStops) {
        return new Course(
                id,
                ownerMemberId,
                info,
                startLocation,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                nextStops,
                tags
        );
    }

    public Course withStartLocation(CourseStopPoint startPoint) {
        Coordinate location = startPoint == null
                ? null
                : new Coordinate(startPoint.latitude(), startPoint.longitude());
        return new Course(
                id,
                ownerMemberId,
                info,
                location,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                stops,
                tags
        );
    }

    public Course withTags(List<Tag> nextTags) {
        return new Course(
                id,
                ownerMemberId,
                info,
                startLocation,
                distanceMeters,
                saveCount,
                createdAt,
                updatedAt,
                stops,
                nextTags
        );
    }

    public int stopCount() {
        return stops.size();
    }

    public int segmentCount() {
        return Math.max(stops.size() - 1, 0);
    }

    public int routeDistanceMeters() {
        return stops.stream()
                .mapToInt(stop -> stop.distanceToNext() == null ? 0 : stop.distanceToNext())
                .sum();
    }

    public int routeDurationSeconds() {
        return stops.stream()
                .mapToInt(stop -> stop.durationToNext() == null ? 0 : stop.durationToNext())
                .sum();
    }
}
