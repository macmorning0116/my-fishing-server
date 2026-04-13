package com.yechan.fishing.fishing_api.domain.map.service;

import com.yechan.fishing.fishing_api.domain.map.dto.PlaceSearchResponse;
import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.global.external.naver.NaverAddress;
import com.yechan.fishing.fishing_api.global.external.naver.NaverReverseGeocodeClient;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MapService {

  private final NaverReverseGeocodeClient naverClient;
  private final WebClient kakaoWebClient;

  public MapService(
      NaverReverseGeocodeClient naverClient,
      @Value("${kakao.api.rest-key}") String kakaoRestApiKey) {
    this.naverClient = naverClient;
    this.kakaoWebClient =
        WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK " + kakaoRestApiKey)
            .build();
  }

  @Cacheable(
      value = "reverseGeocode",
      key = "T(Math).round(#lat * 1000) + '_' + T(Math).round(#lng * 1000)")
  public ReverseGeocodeResponse getAddress(double lat, double lng) {
    NaverAddress address = naverClient.reverseGeocode(lat, lng);
    return new ReverseGeocodeResponse(address.sido(), address.sigungu(), address.dong());
  }

  @SuppressWarnings("unchecked")
  public List<PlaceSearchResponse> searchPlace(String query) {
    Map<String, Object> result =
        kakaoWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("size", 5)
                        .build())
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    if (result == null || result.get("documents") == null) {
      return List.of();
    }

    List<Map<String, Object>> docs = (List<Map<String, Object>>) result.get("documents");
    return docs.stream()
        .map(
            doc ->
                new PlaceSearchResponse(
                    (String) doc.get("place_name"),
                    (String) doc.get("address_name"),
                    Double.parseDouble((String) doc.get("y")),
                    Double.parseDouble((String) doc.get("x"))))
        .toList();
  }
}
