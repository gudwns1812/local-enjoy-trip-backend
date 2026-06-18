package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.external.ExternalClientTestSupport.FakeHttpClient;
import com.ssafy.enjoytrip.core.domain.Attraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TourApiClientTest {
    @DisplayName("관광지 검색은 키워드가 비면 지역 기반 목록을 사용하고 XML 항목을 매핑한다")
    @Test
    void searchUsesAreaBasedListWhenKeywordIsBlankAndMapsXmlItems() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items>
                  <item>
                    <contentid>123</contentid><title>한라산</title><addr1>제주 제주시</addr1><addr2>산록</addr2>
                    <zipcode>63000</zipcode><tel>064</tel><firstimage>img1</firstimage>
                    <firstimage2>img2</firstimage2>
                    <readcount>42</readcount><areacode>39</areacode><sigungucode>4</sigungucode>
                    <mapy>33.3617</mapy><mapx>126.5292</mapx><mlevel>6</mlevel>
                    <contenttypeid>12</contenttypeid>
                  </item>
                </items></body></response>
                """);
        TourApiClient client = new TourApiClient(http, "tour-key");

        List<Attraction> results = client.search("39", "4", "12", " ");

        assertThat(results).containsExactly(new Attraction(
                123L, "한라산", "제주 제주시", "산록", "63000", "064", "img1", "img2",
                42, 39, 4, 33.3617, 126.5292, "6", "12", "",
                0, 0.0, 0, List.of(), false, null
        ));
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("/areaBasedList2?")
                .contains("serviceKey=tour-key")
                .contains("areaCode=39")
                .contains("sigunguCode=4")
                .contains("contentTypeId=12")
                .doesNotContain("keyword=");
    }

    @DisplayName("관광지 검색은 키워드 엔드포인트를 사용하고 한글 키워드를 URL 인코딩한다")
    @Test
    void searchUsesKeywordEndpointAndUrlEncodesKoreanKeyword() throws Exception {
        FakeHttpClient http = new FakeHttpClient()
                .enqueue(200, "<response><body><items /></body></response>");
        TourApiClient client = new TourApiClient(http, "tour-key");

        assertThat(client.search("1", "", "", "서울 여행")).isEmpty();

        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("/searchKeyword2?")
                .contains("keyword=서울 여행")
                .contains("areaCode=1")
                .doesNotContain("sigunguCode=")
                .doesNotContain("contentTypeId=");
    }

    @DisplayName("주변 관광지 검색은 위치 기반 요청을 만들고 잘못된 숫자는 기본값으로 처리한다")
    @Test
    void searchAroundBuildsLocationBasedRequestAndDefaultsMalformedNumbers() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items><item>
                  <contentid>bad-id</contentid><title>좌표 오류 관광지</title>
                  <readcount>NaN</readcount><areacode>bad</areacode><sigungucode></sigungucode>
                  <mapy>not-a-number</mapy><mapx></mapx>
                </item></items></body></response>
                """);
        TourApiClient client = new TourApiClient(http, "tour-key");

        List<Attraction> results = client.searchAround("126.9", "37.5", "1500", "15", "궁");

        assertThat(results).singleElement().satisfies(item -> {
            assertThat(item.id()).isNull();
            assertThat(item.readcount()).isZero();
            assertThat(item.sidoCode()).isZero();
            assertThat(item.gugunCode()).isZero();
            assertThat(item.latitude()).isZero();
            assertThat(item.longitude()).isZero();
        });
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("/locationBasedList2?")
                .contains("mapX=126.9")
                .contains("mapY=37.5")
                .contains("radius=1500")
                .contains("contentTypeId=15")
                .contains("keyword=궁");
    }

    @DisplayName("API 키가 없으면 어떤 HTTP 호출도 하기 전에 예외를 던진다")
    @Test
    void throwsWhenApiKeyIsMissingBeforeAnyHttpCall() {
        FakeHttpClient http = new FakeHttpClient();
        TourApiClient client = new TourApiClient(http, "");

        assertThatThrownBy(() -> client.search("", "", "", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tour API 키가 없습니다");
        assertThat(http.requests()).isEmpty();
        assertThat(client.isConfigured()).isFalse();
    }

    @DisplayName("HTTP 오류와 잘못된 XML은 IOException을 던진다")
    @Test
    void throwsIOExceptionForHttpErrorAndMalformedXml() {
        TourApiClient httpErrorClient = new TourApiClient(
                new FakeHttpClient().enqueue(503, "unavailable"),
                "tour-key"
        );

        assertThatThrownBy(() -> httpErrorClient.search("", "", "", ""))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Tour API HTTP 오류: 503");

        TourApiClient malformedClient = new TourApiClient(
                new FakeHttpClient().enqueue(200, "<response><item>"),
                "tour-key"
        );

        assertThatThrownBy(() -> malformedClient.search("", "", "", ""))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Tour API 응답을 파싱하지 못했습니다");
    }
}
