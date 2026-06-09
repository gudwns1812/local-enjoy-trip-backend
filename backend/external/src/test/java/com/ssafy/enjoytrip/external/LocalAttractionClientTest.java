package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.domain.Attraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalAttractionClientTest {
    @DisplayName("로컬 관광지 검색은 JSON을 매핑하고 지역과 콘텐츠 타입 및 키워드로 필터링한다")
    @Test
    void searchMapsLocalJsonAndFiltersAreaContentTypeAndKeyword() {
        LocalAttractionClient client = new LocalAttractionClient();

        List<Attraction> results = client.search("1", "", "12", "궁");

        assertThat(results).containsExactly(new Attraction(
                1L,
                "경복궁",
                "서울특별시 종로구 사직로 161",
                "서울특별시 종로구 세종로 1-1",
                "",
                "02-3700-3900",
                "",
                "",
                0,
                1,
                0,
                37.579617,
                126.977041,
                "",
                "12",
                "조선 시대 궁궐",
                0,
                0.0,
                0,
                List.of(),
                false,
                null
        ));
    }

    @DisplayName("로컬 관광지 검색은 도로명 주소가 비면 지번 주소를 쓰고 빈 제목은 건너뛴다")
    @Test
    void searchUsesJibunAddressWhenRoadAddressIsBlankAndSkipsBlankTitles() {
        LocalAttractionClient client = new LocalAttractionClient();

        List<Attraction> results = client.search("39", "", "12", "오름");

        assertThat(results).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(2L);
            assertThat(item.title()).isEqualTo("성산일출봉");
            assertThat(item.addr1()).isEqualTo("제주특별자치도 서귀포시 성산읍 성산리 1");
            assertThat(item.addr2()).isEmpty();
            assertThat(item.sidoCode()).isEqualTo(39);
            assertThat(item.latitude()).isZero();
            assertThat(item.longitude()).isEqualTo(126.9425);
        });

        assertThat(client.search("", "", "12", "")).extracting(Attraction::title)
                .doesNotContain("");
    }

    @DisplayName("주변 검색은 거리로 필터링하고 좌표가 없으면 일반 검색으로 대체한다")
    @Test
    void searchAroundFiltersByDistanceAndFallsBackToSearchWhenCoordinatesMissing() {
        LocalAttractionClient client = new LocalAttractionClient();

        assertThat(client.searchAround("126.9770", "37.5796", "100", "12", "궁"))
                .extracting(Attraction::title)
                .containsExactly("경복궁");

        assertThat(client.searchAround("", "", "100", "12", "해수욕장"))
                .extracting(Attraction::title)
                .containsExactly("해운대해수욕장");
    }
}
