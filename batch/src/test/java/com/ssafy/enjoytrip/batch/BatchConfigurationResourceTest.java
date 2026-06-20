package com.ssafy.enjoytrip.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class BatchConfigurationResourceTest {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Test
    @DisplayName("batch application.yml은 .env를 직접 import하지 않고 db-core.yml을 재사용한다")
    void batchApplicationImportsDbCoreWithoutDotEnv() throws Exception {
        String content = resolver.getResource("classpath:application.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(content)
                .contains("db-core.yml")
                .doesNotContain("optional:file:")
                .doesNotContain(".env")
                .doesNotContain("spring:\n  datasource:");
    }

    @Test
    @DisplayName("batch 런타임은 support logging 리소스를 classpath에서 사용한다")
    void supportLoggingResourceIsDiscoverable() {
        assertThat(resolver.getResource("classpath:logback-spring.xml").exists()).isTrue();
    }

}
