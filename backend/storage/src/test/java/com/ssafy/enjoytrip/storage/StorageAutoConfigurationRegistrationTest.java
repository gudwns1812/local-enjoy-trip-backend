package com.ssafy.enjoytrip.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class StorageAutoConfigurationRegistrationTest {
    @DisplayName("스토리지 설정은 Boot 자동 설정으로 등록된다")
    @Test
    void storageConfigurationIsRegisteredAsBootAutoConfiguration() throws IOException {
        assertThat(StorageConfiguration.class).hasAnnotation(AutoConfiguration.class);

        try (var input = getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")) {
            assertThat(input).isNotNull();
            assertThat(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .contains(StorageConfiguration.class.getName());
        }
    }
}
