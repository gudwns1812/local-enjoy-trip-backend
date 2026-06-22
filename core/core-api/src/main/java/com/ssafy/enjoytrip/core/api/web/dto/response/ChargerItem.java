package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.service.ChargerResult;
public record ChargerItem(
        String statId,
        String statNm,
        String chgerId,
        String chgerType,
        String addr,
        String location,
        Double lat,
        Double lng,
        String useTime,
        String busiNm,
        String busiCall,
        String stat
) {
    public static ChargerItem from(ChargerResult item) {
        return new ChargerItem(
                item.statId(),
                item.statNm(),
                item.chgerId(),
                item.chgerType(),
                item.addr(),
                item.location(),
                item.lat(),
                item.lng(),
                item.useTime(),
                item.busiNm(),
                item.busiCall(),
                item.stat()
        );
    }
}
