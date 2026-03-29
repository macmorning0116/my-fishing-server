package com.yechan.fishing.fishing_api.domain.map.controller;

import com.yechan.fishing.fishing_api.domain.map.dto.ReverseGeocodeResponse;
import com.yechan.fishing.fishing_api.domain.map.service.MapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MapController.class)
class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MapService mapService;

    @Test
    void reverseGeocode_returnsWrappedSuccessResponse() throws Exception {
        given(mapService.getAddress(eq(37.5665), eq(126.9780)))
                .willReturn(new ReverseGeocodeResponse("서울특별시", "중구", "태평로1가"));

        mockMvc.perform(get("/v1/map/reverse-geocode")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sido").value("서울특별시"))
                .andExpect(jsonPath("$.data.sigungu").value("중구"))
                .andExpect(jsonPath("$.data.dong").value("태평로1가"));
    }

    @Test
    void reverseGeocode_whenLongitudeMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/map/reverse-geocode")
                        .param("lat", "37.5665"))
                .andExpect(status().isBadRequest());
    }
}
