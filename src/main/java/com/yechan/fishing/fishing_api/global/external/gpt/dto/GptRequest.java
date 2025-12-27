package com.yechan.fishing.fishing_api.global.external.gpt.dto;

import java.util.List;

public record GptRequest(
        String model,
        List<Input> input
) {

    public record Input(
            String role,
            List<Content> content
    ) {}

    public sealed interface Content permits TextContent, ImageContent {}

    public record TextContent(
            String type,
            String text
    ) implements Content {}

    public record ImageContent (
            String type,
            String image_url,
            String detail
    ) implements Content {}

}
