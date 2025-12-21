package com.yechan.fishing.fishing_api.domain.map.service;

import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.global.external.naver.NaverAddress;
import com.yechan.fishing.fishing_api.global.external.naver.NaverReverseGeocodeClient;
import org.springframework.stereotype.Service;

@Service
public class MapService {

    private final NaverReverseGeocodeClient naverClient;

    public MapService(NaverReverseGeocodeClient naverClient) {
        this.naverClient = naverClient;
    }

    public ReverseGeocodeResponse reverseGeocodeResponse(double lat, double lng) {
        NaverAddress address = naverClient.reverseGeocode(lat, lng);

        return new ReverseGeocodeResponse(
                address.sido(),
                address.sigungu(),
                address.dong()
        );
    }
}
