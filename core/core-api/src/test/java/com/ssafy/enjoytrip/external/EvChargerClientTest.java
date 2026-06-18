package com.ssafy.enjoytrip.external;

import com.ssafy.enjoytrip.external.ExternalClientTestSupport.FakeHttpClient;
import com.ssafy.enjoytrip.core.domain.ChargerItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvChargerClientTest {
    @DisplayName("충전소 조회는 XML을 매핑하고 키워드 필터와 페이지 보정을 적용한다")
    @Test
    void findChargersMapsXmlFiltersKeywordAndClampsPaging() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items>
                  <item>
                    <statId>ST001</statId><statNm>강남 충전소</statNm><chgerId>01</chgerId>
                    <chgerType>06</chgerType>
                    <addr>서울 강남구 테헤란로</addr><location>지하 1층</location><lat>37.5</lat><lng>127.0</lng>
                    <useTime>24시간</useTime><busiNm>환경부</busiNm><busiCall>1661</busiCall><stat>2</stat>
                  </item>
                  <item>
                    <statId>ST002</statId><statNm>부산 충전소</statNm><lat>bad</lat><lng></lng>
                  </item>
                </items></body></response>
                """);
        EvChargerClient client = new EvChargerClient(http, "ev-key");

        List<ChargerItem> results = client.findChargers("11", "강남", -1, 999);

        assertThat(results).containsExactly(new ChargerItem(
                "ST001", "강남 충전소", "01", "06", "서울 강남구 테헤란로", "지하 1층",
                37.5, 127.0, "24시간", "환경부", "1661", "2"
        ));
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("serviceKey=ev-key")
                .contains("pageNo=1")
                .contains("numOfRows=500")
                .contains("zcode=11");
    }

    @DisplayName("충전소 조회는 빈 키워드면 모든 행을 유지하고 잘못된 좌표는 기본값으로 처리한다")
    @Test
    void findChargersKeepsAllRowsWhenKeywordBlankAndDefaultsInvalidCoordinates() throws Exception {
        FakeHttpClient http = new FakeHttpClient().enqueue(200, """
                <response><body><items>
                  <item><statNm>첫번째</statNm><lat></lat><lng>bad</lng></item>
                  <item><statNm>두번째</statNm><lat>35.1</lat><lng>129.1</lng></item>
                </items></body></response>
                """);
        EvChargerClient client = new EvChargerClient(http, "ev-key");

        List<ChargerItem> results = client.findChargers("", "", 3, 1);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).lat()).isZero();
        assertThat(results.get(0).lng()).isZero();
        assertThat(results.get(1).lat()).isEqualTo(35.1);
        assertThat(results.get(1).lng()).isEqualTo(129.1);
        String uri = URLDecoder.decode(http.requests().getFirst().uri().toString(), StandardCharsets.UTF_8);
        assertThat(uri)
                .contains("pageNo=3")
                .contains("numOfRows=10")
                .doesNotContain("zcode=");
    }

    @DisplayName("API 키가 없으면 HTTP 호출 없이 예외를 던진다")
    @Test
    void throwsWhenApiKeyMissingWithoutCallingHttp() {
        FakeHttpClient http = new FakeHttpClient();
        EvChargerClient client = new EvChargerClient(http, "");

        assertThatThrownBy(() -> client.findChargers("11", "", 1, 10))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EV 충전기 API 키가 없습니다");
        assertThat(http.requests()).isEmpty();
    }

    @DisplayName("HTTP 오류와 잘못된 XML 및 전송 실패는 IOException을 던진다")
    @Test
    void throwsIOExceptionForHttpErrorMalformedXmlAndTransportFailure() {
        EvChargerClient httpErrorClient = new EvChargerClient(
                new FakeHttpClient().enqueue(500, "error"),
                "ev-key"
        );

        assertThatThrownBy(() -> httpErrorClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("EV API HTTP 오류: 500");

        EvChargerClient malformedClient = new EvChargerClient(
                new FakeHttpClient().enqueue(200, "<response><item>"),
                "ev-key"
        );

        assertThatThrownBy(() -> malformedClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("EV 충전기 API 응답을 파싱하지 못했습니다");

        EvChargerClient transportClient = new EvChargerClient(
                new FakeHttpClient().enqueueIOException("오프라인 상태입니다."),
                "ev-key"
        );

        assertThatThrownBy(() -> transportClient.findChargers("", "", 1, 10))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("오프라인 상태입니다");
    }
}
