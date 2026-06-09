package com.ssafy.enjoytrip.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionTest {

    @DisplayName("표준 생성자는 명시 상세 필드와 record 동작을 보존한다")
    @Test
    void canonicalConstructorPreservesExplicitDetailFieldsAndRecordBehavior() {
        Attraction attraction = detailedAttraction("12", "Night beach overview");
        Attraction same = detailedAttraction("12", "Night beach overview");
        Attraction differentOverview = detailedAttraction("12", "Different overview");

        assertAll(
                () -> assertEquals(same, attraction),
                () -> assertEquals(same.hashCode(), attraction.hashCode()),
                () -> assertNotEquals(differentOverview, attraction),
                () -> assertEquals("12", attraction.contentTypeId()),
                () -> assertEquals("Night beach overview", attraction.overview()),
                () -> assertTrue(attraction.toString().contains("contentTypeId=12")),
                () -> assertTrue(attraction.toString().contains("overview=Night beach overview"))
        );
    }

    @DisplayName("표준 생성자는 참조 타입 null 값을 허용한다")
    @Test
    void canonicalConstructorAllowsNullReferenceValues() {
        Attraction attraction = new Attraction(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                0.0,
                0,
                null,
                false,
                null
        );

        assertAll(
                () -> assertNull(attraction.id()),
                () -> assertNull(attraction.title()),
                () -> assertNull(attraction.latitude()),
                () -> assertNull(attraction.longitude()),
                () -> assertNull(attraction.contentTypeId()),
                () -> assertNull(attraction.overview())
        );
    }

    private static Attraction detailedAttraction(String contentTypeId, String overview) {
        return new Attraction(
                1L,
                "Gwangalli Beach",
                "Busan",
                "Suyeong-gu",
                "48283",
                "051-000-0000",
                "main.jpg",
                "thumb.jpg",
                42,
                6,
                12,
                35.1532,
                129.1187,
                "6",
                contentTypeId,
                overview,
                0,
                0.0,
                0,
                java.util.List.of(),
                false,
                null
        );
    }
}
