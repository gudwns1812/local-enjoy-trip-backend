package com.ssafy.enjoytrip.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.boot.DefaultApplicationArguments;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttractionEmbeddingBackfillJobLauncherTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-05T00:00:00Z"),
            ZoneOffset.UTC
    );

    @DisplayName("Spring Boot 옵션 인자로 배치 작업 파라미터를 만든다")
    @Test
    void buildsJobParametersFromSpringBootOptionArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--dryRun=true",
                "--limit=10"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("sourceVersion")).isEqualTo("tourapi-2026-06-05");
        assertThat(parameters.getString("dryRun")).isEqualTo("true");
        assertThat(parameters.getLong("limit")).isEqualTo(10L);
        assertThat(parameters.getLong("run.id")).isEqualTo(FIXED_CLOCK.millis());
    }

    @DisplayName("원시 배치 형식 인자로 작업 파라미터를 만든다")
    @Test
    void buildsJobParametersFromRawBatchStyleArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "sourceVersion=tourapi-2026-06-05",
                "dryRun=false",
                "limit=3"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("sourceVersion")).isEqualTo("tourapi-2026-06-05");
        assertThat(parameters.getString("dryRun")).isEqualTo("false");
        assertThat(parameters.getLong("limit")).isEqualTo(3L);
    }

    @DisplayName("값 없는 dry-run 플래그는 true로 해석한다")
    @Test
    void dryRunFlagWithoutValueMeansTrue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--dryRun"
        );

        JobParameters parameters = AttractionEmbeddingBackfillJobLauncher.buildJobParameters(
                args,
                FIXED_CLOCK
        );

        assertThat(parameters.getString("dryRun")).isEqualTo("true");
        assertThat(parameters.getLong("limit")).isZero();
    }

    @DisplayName("배치 실행을 열기 전에 sourceVersion 필수값을 검증한다")
    @Test
    void sourceVersionIsRequiredBeforeOpeningBatchExecution() {
        DefaultApplicationArguments args = new DefaultApplicationArguments("--dryRun=true");

        assertThatThrownBy(() -> AttractionEmbeddingBackfillJobLauncher.buildJobParameters(args, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceVersion 작업 파라미터가 필요합니다");
    }

    @DisplayName("배치 실행을 열기 전에 음수 limit을 거부한다")
    @Test
    void negativeLimitIsRejectedBeforeOpeningBatchExecution() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--sourceVersion=tourapi-2026-06-05",
                "--limit=-1"
        );

        assertThatThrownBy(() -> AttractionEmbeddingBackfillJobLauncher.buildJobParameters(args, FIXED_CLOCK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit 작업 파라미터");
    }
}
