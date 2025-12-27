package com.yechan.fishing.fishing_api.global.external.weather;

import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.domain.weather.dto.WeatherResponse;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.external.weather.dto.OpenWeatherResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WeatherClient {

    private final WebClient webClient;
    private final OpenWeatherApiProperties props;

    public WeatherClient(
            @Qualifier("openWeatherWebClient") WebClient webClient,
            OpenWeatherApiProperties props
    ) {
        this.webClient = webClient;
        this.props = props;
    }

    private OpenWeatherResponse fetchRawWeather(double lat, double lng) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lng)
                            .queryParam("units", "metric")
                            .queryParam("appid", props.getKey())
                            .build()
                    )
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new FishingException(ErrorCode.WEATHER_API_ERROR))
                    )
                    .bodyToMono(OpenWeatherResponse.class)
                    .block();
        } catch (Exception e) {
            throw new FishingException(ErrorCode.WEATHER_API_ERROR);
        }
    }

    public WeatherResponse getWeather(double lat, double lng) {
        OpenWeatherResponse res = fetchRawWeather(lat, lng);
        return toWeatherResponse(res);
    }

    public GptWeatherContext getGptWeatherContext(double lat, double lng) {
        OpenWeatherResponse res = fetchRawWeather(lat, lng);
        return toGptWeatherContext(lat, lng, res);
    }


    private WeatherResponse toWeatherResponse(OpenWeatherResponse res) {
        validateResponse(res);
        return new WeatherResponse(
                res.getMain().getTemp(),
                res.getWeather().get(0).getMain(),
                res.getSys().getSunrise(),
                res.getSys().getSunset()
        );
    }

    private GptWeatherContext toGptWeatherContext(
            double lat,
            double lng,
            OpenWeatherResponse res
    ) {
        validateResponse(res);

        return new GptWeatherContext(
                lat,
                lng,
                res.getDt(),
                res.getMain().getTemp(),
                res.getMain().getFeelsLike(),
                res.getMain().getHumidity(),
                res.getWind().getSpeed(),
                res.getWind().getDeg(),
                res.getClouds().getAll(),
                res.getWeather().get(0).getMain(),
                res.getWeather().get(0).getDescription(),
                res.getSys().getSunrise(),
                res.getSys().getSunset()
        );
    }

    private static void validateResponse(OpenWeatherResponse res) {
        if (res == null
            || res.getMain() == null
                || res.getWeather() == null
                || res.getWeather().isEmpty()
                || res.getSys() == null){
            throw new FishingException(ErrorCode.WEATHER_API_INVALID_DATA);
        }
    }


}
