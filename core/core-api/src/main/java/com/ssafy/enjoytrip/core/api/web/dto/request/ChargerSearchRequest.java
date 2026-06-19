package com.ssafy.enjoytrip.core.api.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ChargerSearchRequest(
        String zcode,
        String keyword,
        @Min(1) Integer pageNo,
        @Min(1) @Max(500) Integer numOfRows
) {
    public String normalizedZcode() {
        return trimToNull(zcode);
    }

    public String normalizedKeyword() {
        return trimToNull(keyword);
    }

    public int pageNoOrDefault() {
        if (pageNo == null) {
            return 1;
        }
        return pageNo;
    }

    public int numOfRowsOrDefault() {
        if (numOfRows == null) {
            return 150;
        }
        return numOfRows;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
