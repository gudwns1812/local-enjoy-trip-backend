package com.ssafy.enjoytrip.core.api.worker.popularity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsMaintenanceService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttractionPopularitySchedulerTest {

    @DisplayName("AttractionPopularityScheduler는 실행 중인 flush가 있으면 중복 실행을 건너뛴다")
    @Test
    void skipsOverlappingFlushWithSingleFlightGuard() throws Exception {
        AttractionPopularityStatsMaintenanceService maintenanceService =
                mock(AttractionPopularityStatsMaintenanceService.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            release.await(1, TimeUnit.SECONDS);
            return 1;
        }).when(maintenanceService).flushBufferedFavoriteDeltas();
        AttractionPopularityScheduler scheduler = new AttractionPopularityScheduler(maintenanceService);

        Thread firstRun = new Thread(scheduler::flushFavoriteDeltas);
        firstRun.start();
        assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

        scheduler.flushFavoriteDeltas();
        release.countDown();
        firstRun.join(1_000);

        verify(maintenanceService, times(1)).flushBufferedFavoriteDeltas();
    }
}
