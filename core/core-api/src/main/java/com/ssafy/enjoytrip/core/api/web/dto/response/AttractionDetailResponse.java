package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import java.util.List;

public record AttractionDetailResponse(
        Long id,
        String title,
        String address,
        String addressDetail,
        String zipcode,
        String tel,
        String imageUrl,
        Integer readcount,
        Double latitude,
        Double longitude,
        String contentTypeId,
        String overview,
        int saveCount,
        double ratingAverage,
        int ratingCount,
        List<Tag> tags,
        boolean saved,
        Integer myRating
) {
    public AttractionDetailResponse(Attraction attraction) {
        this(
                attraction.id(),
                attraction.title(),
                attraction.addr1(),
                attraction.addr2(),
                attraction.zipcode(),
                attraction.tel(),
                attraction.primaryImageUrl(),
                attraction.readcount(),
                attraction.latitude(),
                attraction.longitude(),
                attraction.contentTypeId(),
                attraction.overview(),
                attraction.saveCount(),
                attraction.ratingAverage(),
                attraction.ratingCount(),
                tags(attraction),
                attraction.saved(),
                attraction.myRating()
        );
    }

    private static List<Tag> tags(Attraction attraction) {
        return attraction.tags().stream()
                .map(tag -> new Tag(tag.id(), tag.name()))
                .toList();
    }

    public record Tag(
            Long id,
            String name
    ) {
    }
}
