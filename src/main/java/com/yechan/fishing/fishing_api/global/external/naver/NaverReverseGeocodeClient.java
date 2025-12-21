package com.yechan.fishing.fishing_api.global.external.naver;

import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.external.naver.dto.NaverReverseGeocodeResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class NaverReverseGeocodeClient {

    private final WebClient webClient;

    public NaverReverseGeocodeClient(
            @Qualifier("naverWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    public NaverAddress reverseGeocode(double lat, double lng) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/map-reversegeocode/v2/gc")
                        .queryParam("coords", lng + ", " + lat)
                        .queryParam("output", "json")
                        .queryParam("orders", "legalcode,admcode,addr,roadaddr")
                        .build())
                .retrieve()
                .bodyToMono(NaverReverseGeocodeResponse.class)
                .map(this::extractAddress)
                .block();
    }


    private NaverAddress extractAddress(NaverReverseGeocodeResponse response) {
        if (response.getResults() == null || response.getResults().isEmpty()) {
            throw new FishingException(ErrorCode.NAVER_API_ERROR);
        }

        var region = response.getResults().get(0).getRegion();

        return new NaverAddress(
                region.getArea1().getName(),
                region.getArea2().getName(),
                region.getArea3().getName()
        );

    }

}
