package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.ChargerResult;
import java.util.List;

public record ChargersResponse(List<ChargerItem> chargers) {
    public static ChargersResponse from(List<ChargerResult> chargers) {
        return new ChargersResponse(chargers.stream()
                .map(ChargerItem::from)
                .toList());
    }
}
