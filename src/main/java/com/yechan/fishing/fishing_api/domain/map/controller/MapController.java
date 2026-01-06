package com.yechan.fishing.fishing_api.domain.map.controller;

import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeRequest;
import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.domain.map.service.MapService;
import com.yechan.fishing.fishing_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/reverse-geocode")
    public ApiResponse<ReverseGeocodeResponse> reverseGeocode(
            @Valid ReverseGeocodeRequest request
            ){
        return ApiResponse.success(
                mapService.getAddress(request.lat(), request.lng())
        );
    }

}
