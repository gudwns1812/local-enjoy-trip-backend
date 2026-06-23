package com.ssafy.enjoytrip.external.courseorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

class SpringAiCourseOrderRecommendationClientTest {

    @DisplayName("코스 순서 AI 클라이언트는 strict JSON 아이템 id 배열을 반환한다")
    @Test
    void returnsStrictJsonItemIds() {
        SpringAiCourseOrderRecommendationClient client = clientReturning(
                "{\"orderedItemIds\":[102,101]}"
        );

        CourseOrderRecommendationResult result = client.recommend(request());

        assertThat(result.orderedItemIds()).containsExactly(102L, 101L);
    }

    @DisplayName("코스 순서 AI 클라이언트는 설명으로 감싼 JSON을 malformed 응답으로 거부한다")
    @Test
    void rejectsWrappedJsonResponse() {
        SpringAiCourseOrderRecommendationClient client = clientReturning(
                "Here is the order: {\"orderedItemIds\":[102,101]}"
        );

        assertThatThrownBy(() -> client.recommend(request()))
                .isInstanceOfSatisfying(CourseOrderRecommendationException.class,
                        exception -> assertThat(exception.reason())
                                .isEqualTo(CourseOrderRecommendationException.Reason.MALFORMED_RESPONSE));
    }

    @DisplayName("코스 순서 AI 클라이언트는 정수가 아닌 id를 malformed 응답으로 거부한다")
    @Test
    void rejectsNonIntegralIds() {
        SpringAiCourseOrderRecommendationClient client = clientReturning(
                "{\"orderedItemIds\":[101.5,102]}"
        );

        assertThatThrownBy(() -> client.recommend(request()))
                .isInstanceOfSatisfying(CourseOrderRecommendationException.class,
                        exception -> assertThat(exception.reason())
                                .isEqualTo(CourseOrderRecommendationException.Reason.MALFORMED_RESPONSE));
    }

    @DisplayName("코스 순서 AI 클라이언트는 추가 필드를 malformed 응답으로 거부한다")
    @Test
    void rejectsExtraFields() {
        SpringAiCourseOrderRecommendationClient client = clientReturning(
                "{\"orderedItemIds\":[102,101],\"reason\":\"because\"}"
        );

        assertThatThrownBy(() -> client.recommend(request()))
                .isInstanceOfSatisfying(CourseOrderRecommendationException.class,
                        exception -> assertThat(exception.reason())
                                .isEqualTo(CourseOrderRecommendationException.Reason.MALFORMED_RESPONSE));
    }

    private static CourseOrderRecommendationRequest request() {
        return new CourseOrderRecommendationRequest(
                "course-1",
                List.of(
                        new CourseOrderRecommendationItem(
                                101L,
                                "ATTRACTION",
                                10L,
                                "첫 장소",
                                1,
                                1,
                                37.0,
                                127.0
                        ),
                        new CourseOrderRecommendationItem(
                                102L,
                                "ATTRACTION",
                                20L,
                                "두 번째 장소",
                                1,
                                2,
                                37.1,
                                127.1
                        )
                )
        );
    }

    private static SpringAiCourseOrderRecommendationClient clientReturning(String content) {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(content);
        return new SpringAiCourseOrderRecommendationClient(provider(builder), new ObjectMapper());
    }

    private static ObjectProvider<ChatClient.Builder> provider(ChatClient.Builder builder) {
        return new ObjectProvider<>() {
            @Override
            public ChatClient.Builder getObject(Object... args) throws BeansException {
                return builder;
            }

            @Override
            public ChatClient.Builder getObject() throws BeansException {
                return builder;
            }

            @Override
            public ChatClient.Builder getIfAvailable() throws BeansException {
                return builder;
            }
        };
    }
}
