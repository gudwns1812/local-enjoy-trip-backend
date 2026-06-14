package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;
import com.ssafy.enjoytrip.domain.WeatherSummary;
import com.ssafy.enjoytrip.repository.CourseRepository;
import com.ssafy.enjoytrip.repository.NeighborhoodBriefingGenerator;
import com.ssafy.enjoytrip.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeighborhoodBriefingServiceTest {

    @DisplayName("날씨와 공개 코스 후보를 프롬프트로 전달하고 생성 문장을 반환한다")
    @Test
    void returnsGeneratedBriefingWithWeatherSeasonAndCourseCandidates() {
        CapturingGenerator generator = new CapturingGenerator("한강 저녁 산책 코스 어떠세요?");
        NeighborhoodBriefingService service = service(
                List.of(new WeatherSummary("서울", "맑음", 27, 10, "05:10", "19:50")),
                List.of(new CourseBriefingCandidate("course-1", "한강 저녁 산책", "서울")),
                generator,
                "2026-07-01T00:00:00Z"
        );

        NeighborhoodBriefing result = service.brief("서울");

        assertAll(
                () -> assertEquals("서울", result.region()),
                () -> assertEquals("여름", result.season()),
                () -> assertEquals("한강 저녁 산책 코스 어떠세요?", result.briefing()),
                () -> assertEquals("서울", generator.prompt.region()),
                () -> assertEquals("여름", generator.prompt.season()),
                () -> assertEquals("맑음", generator.prompt.weather().condition()),
                () -> assertEquals(
                        List.of("한강 저녁 산책"),
                        generator.prompt.courseCandidates().stream()
                                .map(CourseBriefingCandidate::title)
                                .toList()
                )
        );
    }

    @DisplayName("LLM 생성기가 예외를 던지면 fallback으로 숨기지 않고 예외를 전파한다")
    @Test
    void propagatesExceptionWhenGeneratorThrows() {
        NeighborhoodBriefingService service = service(
                List.of(new WeatherSummary("서울", "비", 18, 80, "05:10", "19:50")),
                List.of(new CourseBriefingCandidate("course-1", "비 오는 날 북촌", "서울")),
                prompt -> {
                    throw new IllegalStateException("GMS unavailable");
                },
                "2026-04-01T00:00:00Z"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.brief("서울")
        );

        assertEquals("GMS unavailable", exception.getMessage());
    }

    @DisplayName("LLM 생성기가 blank 문장을 반환하면 저장된 코스 기반 fallback 문장을 반환한다")
    @Test
    void returnsCourseFallbackWhenGeneratorReturnsBlank() {
        NeighborhoodBriefingService service = service(
                List.of(new WeatherSummary("서울", "비", 18, 80, "05:10", "19:50")),
                List.of(new CourseBriefingCandidate("course-1", "비 오는 날 북촌", "서울")),
                new CapturingGenerator("   "),
                "2026-04-01T00:00:00Z"
        );

        NeighborhoodBriefing result = service.brief("서울");

        assertEquals("봄", result.season());
        assertTrue(result.briefing().contains("비 오는 날 북촌 코스 어떠세요?"));
    }

    @DisplayName("코스 후보가 비어도 날씨 기반 fallback 문장을 반환하고 생성기를 호출하지 않는다")
    @Test
    void returnsWeatherFallbackWhenCourseCandidatesAreEmpty() {
        CapturingGenerator generator = new CapturingGenerator("호출되면 안 됨");
        NeighborhoodBriefingService service = service(
                List.of(new WeatherSummary("서울", "흐림", 21, 30, "05:10", "19:50")),
                List.of(),
                generator,
                "2026-10-01T00:00:00Z"
        );

        NeighborhoodBriefing result = service.brief("서울");

        assertEquals("가을", result.season());
        assertTrue(result.briefing().contains("저장된 코스가 생기면"));
        assertNull(generator.prompt);
    }

    @DisplayName("월별 현재 계절을 봄 여름 가을 겨울로 결정한다")
    @Test
    void resolvesSeasonByCurrentMonth() {
        assertEquals("봄", season("2026-03-01T00:00:00Z"));
        assertEquals("여름", season("2026-06-01T00:00:00Z"));
        assertEquals("가을", season("2026-09-01T00:00:00Z"));
        assertEquals("겨울", season("2026-12-01T00:00:00Z"));
    }

    private static NeighborhoodBriefingService service(List<WeatherSummary> weather,
                                                       List<CourseBriefingCandidate> candidates,
                                                       NeighborhoodBriefingGenerator generator,
                                                       String instant) {
        WeatherRepository weatherRepository = () -> weather;
        CourseRepository courseRepository = (regionName, limit) -> new ArrayList<>(candidates).stream()
                .limit(limit)
                .toList();

        return new NeighborhoodBriefingService(
                new WeatherService(weatherRepository),
                courseRepository,
                generator,
                new SeasonResolver(fixedClock(instant))
        );
    }

    private static String season(String instant) {
        return new SeasonResolver(fixedClock(instant)).currentSeason();
    }

    private static Clock fixedClock(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
    }

    private static class CapturingGenerator implements NeighborhoodBriefingGenerator {
        private final String result;
        private NeighborhoodBriefingPrompt prompt;

        CapturingGenerator(String result) {
            this.result = result;
        }

        @Override
        public String generate(NeighborhoodBriefingPrompt prompt) {
            this.prompt = prompt;
            return result;
        }
    }
}
