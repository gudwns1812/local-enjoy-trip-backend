package com.ssafy.enjoytrip.core.api.worker.popularity;

import com.ssafy.enjoytrip.core.api.config.CoreApiStorageConfiguration;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityDeltaBuffer;
import com.ssafy.enjoytrip.core.domain.service.AttractionPopularityStatsMaintenanceService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile("worker")
@Import({
        CoreApiStorageConfiguration.class,
        AttractionPopularityDeltaBuffer.class,
        AttractionPopularityStatsMaintenanceService.class
})
public class AttractionPopularityWorkerConfiguration {
}
