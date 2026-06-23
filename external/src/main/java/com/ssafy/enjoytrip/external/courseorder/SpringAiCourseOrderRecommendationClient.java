package com.ssafy.enjoytrip.external.courseorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAiCourseOrderRecommendationClient {
    private static final String SYSTEM_PROMPT = """
            You recommend only the order of existing trip course items.
            Return strict JSON only, with this shape: {"orderedItemIds":[1,2,3]}.
            The response must contain every provided id exactly once.
            Do not add, remove, explain, or wrap items in any other shape.
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectMapper objectMapper;

    public CourseOrderRecommendationResult recommend(CourseOrderRecommendationRequest request) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new CourseOrderRecommendationException(
                    CourseOrderRecommendationException.Reason.PROVIDER_ERROR,
                    "Spring AI ChatClient.Builder is unavailable."
            );
        }

        String content = builder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt(request))
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new CourseOrderRecommendationException(
                    CourseOrderRecommendationException.Reason.BLANK_RESPONSE,
                    "Course order provider returned blank content."
            );
        }

        return parse(content);
    }

    private CourseOrderRecommendationResult parse(String content) {
        try {
            JsonNode root = objectMapper.readerFor(JsonNode.class)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(content.trim());
            if (root == null || !root.isObject()) {
                throw malformed("Response must be a JSON object.");
            }
            if (root.size() != 1) {
                throw malformed("Response must contain only orderedItemIds.");
            }
            JsonNode orderedIdsNode = root.get("orderedItemIds");
            if (orderedIdsNode == null || !orderedIdsNode.isArray()) {
                throw malformed("orderedItemIds must be an array.");
            }

            List<Long> orderedItemIds = new ArrayList<>();
            for (JsonNode idNode : orderedIdsNode) {
                if (!idNode.isIntegralNumber() || !idNode.canConvertToLong()) {
                    throw malformed("orderedItemIds must contain only integral numeric ids.");
                }
                orderedItemIds.add(idNode.longValue());
            }
            return new CourseOrderRecommendationResult(orderedItemIds, "items=" + orderedItemIds.size());
        } catch (JsonProcessingException exception) {
            throw new CourseOrderRecommendationException(
                    CourseOrderRecommendationException.Reason.MALFORMED_RESPONSE,
                    "Course order provider returned malformed JSON.",
                    exception
            );
        }
    }

    private static CourseOrderRecommendationException malformed(String message) {
        return new CourseOrderRecommendationException(
                CourseOrderRecommendationException.Reason.MALFORMED_RESPONSE,
                message
        );
    }

    private static String userPrompt(CourseOrderRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Course id: ").append(request.courseId()).append('\n');
        prompt.append("Items in current order:\n");
        for (CourseOrderRecommendationItem item : request.items()) {
            prompt.append("- id=").append(item.id())
                    .append(", type=").append(item.itemType())
                    .append(", targetId=").append(item.targetId())
                    .append(", title=").append(nullSafe(item.title()))
                    .append(", day=").append(item.day())
                    .append(", currentPosition=").append(item.currentPosition())
                    .append(", latitude=").append(item.latitude())
                    .append(", longitude=").append(item.longitude())
                    .append('\n');
        }
        return prompt.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
