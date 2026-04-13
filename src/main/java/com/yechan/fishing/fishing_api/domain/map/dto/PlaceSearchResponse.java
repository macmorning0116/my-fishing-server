package com.yechan.fishing.fishing_api.domain.map.dto;

public record PlaceSearchResponse(
    String placeName, String address, Double latitude, Double longitude) {}
