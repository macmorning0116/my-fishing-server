package com.yechan.fishing.fishing_api.global.external.naver.dto;

import java.util.List;

public class NaverReverseGeocodeResponse {

    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        private Region region;

        public Region getRegion() {
            return region;
        }
    }

    public static class Region {
        private Area area1;
        private Area area2;
        private Area area3;

        public Area getArea1() {
            return area1;
        }

        public Area getArea2() {
            return area2;
        }

        public Area getArea3() {
            return area3;
        }
    }


    public static class Area {
        private String name;

        public String getName() {
            return name;
        }
    }
}
