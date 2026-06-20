package com.ssafy.enjoytrip.core.api.worker.attraction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttractionPopularityReconcileSchedulerTest {

    @DisplayName("AttractionPopularityReconcileScheduler는 실행 중인 reconcile이 있으면 중복 실행을 건너뛴다")
    @Test
    void skipsOverlappingReconcileWithSingleFlightGuard() throws Exception {
        AttractionPopularityStatsService statsService = mock(AttractionPopularityStatsService.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            release.await(1, TimeUnit.SECONDS);
            return 1;
        }).when(statsService).reconcileFavoriteCounts();
        AttractionPopularityReconcileScheduler scheduler = new AttractionPopularityReconcileScheduler(statsService);

        Thread firstRun = new Thread(scheduler::reconcileFavoriteCounts);
        firstRun.start();
        assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

        scheduler.reconcileFavoriteCounts();
        release.countDown();
        firstRun.join(1_000);

        verify(statsService, times(1)).reconcileFavoriteCounts();
    }
}
