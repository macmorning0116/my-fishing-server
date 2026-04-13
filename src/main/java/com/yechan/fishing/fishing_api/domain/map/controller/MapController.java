package com.yechan.fishing.fishing_api.domain.map.controller;

import com.yechan.fishing.fishing_api.domain.map.dto.PlaceSearchResponse;
import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeRequest;
import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.domain.map.service.MapService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/map")
public class MapController {

  private final MapService mapService;

  public MapController(MapService mapService) {
    this.mapService = mapService;
  }

  @GetMapping("/reverse-geocode")
  public ApiResponse<ReverseGeocodeResponse> reverseGeocode(@Valid ReverseGeocodeRequest request) {
    return ApiResponse.success(mapService.getAddress(request.lat(), request.lng()));
  }

  @GetMapping("/search-place")
  public ApiResponse<List<PlaceSearchResponse>> searchPlace(@RequestParam @NotBlank String query) {
    return ApiResponse.success(mapService.searchPlace(query));
  }
}
