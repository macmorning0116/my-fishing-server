package com.yechan.fishing.fishing_api.domain.map.service;

import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.global.external.naver.NaverAddress;
import com.yechan.fishing.fishing_api.global.external.naver.NaverReverseGeocodeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private NaverReverseGeocodeClient naverClient;

    @InjectMocks
    private MapService mapService;

    @Test
    void getAddress_mapsNaverAddressToResponse() {
        given(naverClient.reverseGeocode(37.5, 127.0))
                .willReturn(new NaverAddress("경기도", "성남시", "분당구"));

        ReverseGeocodeResponse result = mapService.getAddress(37.5, 127.0);

        assertEquals("경기도", result.sido());
        assertEquals("성남시", result.sigungu());
        assertEquals("분당구", result.dong());
        then(naverClient).should().reverseGeocode(37.5, 127.0);
    }
}
