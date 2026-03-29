package com.yechan.fishing.fishing_api.global.external.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OpenWeatherResponse {

    private long dt;
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private Clouds clouds;
    private Sys sys;

    public long getDt() {
        return dt;
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public Sys getSys() {
        return sys;
    }

    /* ===== inner classes ===== */

    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feels_like;
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feels_like;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        private String main;
        private String description;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Wind {
        private double speed;
        private int deg;

        public double getSpeed() {
            return speed;
        }

        public int getDeg() {
            return deg;
        }
    }

    public static class Clouds {
        private int all;

        public int getAll() {
            return all;
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
