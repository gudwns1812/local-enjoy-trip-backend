package com.ssafy.enjoytrip.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class SeasonResolver {
    private final Clock clock;

    public SeasonResolver() {
        this(Clock.systemDefaultZone());
    }

    SeasonResolver(Clock clock) {
        this.clock = clock;
    }

    public String currentSeason() {
        int month = LocalDate.now(clock).getMonthValue();
        if (month >= 3 && month <= 5) {
            return "봄";
        }
        if (month >= 6 && month <= 8) {
            return "여름";
        }
        if (month >= 9 && month <= 11) {
            return "가을";
        }

        return "겨울";
    }
}
