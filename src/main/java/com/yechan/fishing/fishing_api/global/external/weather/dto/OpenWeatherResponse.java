package com.yechan.fishing.fishing_api.global.external.weather.dto;


import java.util.List;

public class OpenWeatherResponse {

    private Main main;
    private List<Weather> weather;
    private Sys sys;

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Sys getSys() {
        return sys;
    }

    public static class Main {
        private double temp;

        public double getTemp() {
            return temp;
        }
    }

    public static class Weather {
        private String main;

        public String getMain() {
            return main;
        }
    }

    public static class Sys {
        private long sunrise;
        private long sunset;

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }
    }

}
