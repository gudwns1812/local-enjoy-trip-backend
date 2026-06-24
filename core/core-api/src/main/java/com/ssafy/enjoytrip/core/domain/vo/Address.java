package com.ssafy.enjoytrip.core.domain.vo;

public record Address(
        String address,
        String addressDetail,
        String zipcode
) {
    public Address {
        address = address == null ? "" : address.trim();
        addressDetail = addressDetail == null ? "" : addressDetail.trim();
        zipcode = zipcode == null ? "" : zipcode.trim();
    }
}
