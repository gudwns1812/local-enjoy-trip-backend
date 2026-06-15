package com.ssafy.enjoytrip.external.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "enjoytrip.ai.embedding.expansion.gms")
public class GmsKeywordExpansionProperties {
    private String url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";
    private String apiKey = "";
    private Duration timeout = Duration.ofSeconds(30);
    private String model = "gpt-5.4";
    private int maxKeywords = 24;

    public void assertLiveReady() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AttractionEmbeddingGatewayException("GMS_KEY_MISSING",
                    "GMS API 키가 없습니다. GMS_KEY를 설정해 "
                            + "enjoytrip.ai.embedding.expansion.gms.api-key 값이 채워지도록 하세요."
            );
        }
        if (url == null || url.isBlank()) {
            throw new AttractionEmbeddingGatewayException(
                    "GMS_CHAT_URL_MISSING",
                    "GMS 채팅 완성 URL이 없습니다."
            );
        }
        if (model == null || model.isBlank()) {
            throw new AttractionEmbeddingGatewayException(
                    "GMS_CHAT_MODEL_MISSING",
                    "GMS 채팅 완성 모델이 없습니다."
            );
        }
        if (maxKeywords <= 0) {
            throw new AttractionEmbeddingGatewayException(
                    "GMS_CHAT_MAX_KEYWORDS_INVALID",
                    "maxKeywords는 양수여야 합니다."
            );
        }
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getMaxKeywords() { return maxKeywords; }
    public void setMaxKeywords(int maxKeywords) { this.maxKeywords = maxKeywords; }
}
