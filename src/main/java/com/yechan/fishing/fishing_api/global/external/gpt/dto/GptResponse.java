package com.yechan.fishing.fishing_api.global.external.gpt.dto;

import java.util.List;

public class GptResponse {

    private List<Output> output;

    public List<Output> getOutput() {
        return output;
    }

    public String getOutputText() {
        if (output == null) return "";
        return output.stream()
                .flatMap(o -> o.getContent().stream())
                .filter(c -> "output_text".equals(c.getType()))
                .map(Content::getText)
                .findFirst()
                .orElse("");
    }

    public static class Output {
        private List<Content> content;
        public List<Content> getContent() { return content; }
    }


    public static class Content {
        private String type;
        private String text;

        public String getType() { return type; }
        public String getText() { return text; }
    }
}
