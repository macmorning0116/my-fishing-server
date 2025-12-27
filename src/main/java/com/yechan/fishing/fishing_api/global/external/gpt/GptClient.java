package com.yechan.fishing.fishing_api.global.external.gpt;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisResponse;
import com.yechan.fishing.fishing_api.domain.analysis.dto.GptWeatherContext;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptRequest;
import com.yechan.fishing.fishing_api.global.external.gpt.dto.GptResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;

@Component
public class GptClient {

    private final WebClient webClient;
    private final OpenAiProperties props;
    private final ObjectMapper objectMapper;

    public GptClient(
            @Qualifier("openAiWebClient") WebClient webClient,
            OpenAiProperties props,
            ObjectMapper objectMapper
    ) {
        this.webClient = webClient;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public AnalysisResponse analyze (
            MultipartFile image,
            GptWeatherContext weather
    ) {
        try {
            String mime = image.getContentType() != null
                    ? image.getContentType()
                    : "image/jpeg";

            String base64 = Base64.getEncoder()
                    .encodeToString(image.getBytes());

            String prompt = buildPrompt(weather);

            GptRequest request = new GptRequest(
                    props.getModel(),
                    List.of(
                            new GptRequest.Input(
                                    "user",
                                    List.of(
                                            new GptRequest.TextContent(
                                                    "input_text",
                                                    prompt
                                            ),
                                            new GptRequest.ImageContent(
                                                    "input_image",
                                                    "data:" + mime + ";base64," + base64,
                                                    "high"
                                            )
                                    )
                            )
                    )
            );

            GptResponse response = webClient.post()
                    .uri("/responses")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GptResponse.class)
                    .block();

            String json = extractJson(response.getOutputText());
            return objectMapper.readValue(json, AnalysisResponse.class);
        } catch (Exception e) {
            throw new FishingException(ErrorCode.GPT_API_ERROR);
        }
    }

    private String buildPrompt(GptWeatherContext w) {
        return """
        당신은 20년차 낚시 고수(포인트 분석 전문가)입니다.
        사용자가 보낸 "사진"과 아래 "환경 정보"를 종합해서, 낚시 포인트를 추천해 주세요.
        
        [말투 규칙]
        - 반드시 존댓말로만 답변해 주세요.
        - 반말/명령조/비속어는 금지합니다.
        
        [출력 규칙]
        - 반드시 아래 JSON "객체" 하나만 출력해 주세요.
        - JSON 외의 설명 문장, 마크다운, 코드블록(```), 주석은 절대 출력하지 마세요.
        - 숫자는 number로 출력하세요(따옴표로 감싸지 마세요).
        - points는 2~4개로 출력해 주세요.
        - 좌표계: 이미지 좌상단이 (0,0), 우하단이 (1,1)인 "비율 좌표"입니다.
        - radius는 0.05 ~ 0.20 범위로 출력해 주세요.
        
        [JSON 스키마]
        {
          "summary": string,
          "points": [
            { "x": number, "y": number, "radius": number, "reason": string }
          ],
          "tackle": string,
          "strategy": string
        }
        
        [작성 가이드]
        - summary: 현재 상황을 2~3문장으로 요약(날씨/바람/수온 추정/활성도 추정 포함)
        - reason: 왜 그 지점이 유리한지(그늘, 수초, 브레이크라인, 유입수, 바람 맞는 면 등)
        - tackle: 채비를 구체적으로(대상어/라인 파운드/훅/싱커/루어 종류)
        - strategy: 운용법을 구체적으로(캐스팅 각도, 수심층, 릴링 속도, 스테이/저킹, 탐색 순서)
        
        [환경 정보]
        - lat: %f
        - lng: %f
        - timestamp: %d
        - temp(°C): %.1f
        - feelsLike(°C): %.1f
        - humidity(%%): %d
        - windSpeed(m/s): %.1f
        - windDeg: %d
        - cloudiness(%%): %d
        - weatherMain: %s
        - weatherDesc: %s
        - sunrise: %d
        - sunset: %d
        """.formatted(
                w.lat(), w.lng(),
                w.timestamp(),
                w.temperature(), w.feelsLike(),
                w.humidity(),
                w.windSpeed(), w.windDeg(),
                w.cloudiness(),
                w.weatherMain(), w.weatherDesc(),
                w.sunrise(), w.sunset()
        );
    }


    private String extractJson(String s) {
        if (s == null) return "{}";
        int l = s.indexOf('{');
        int r = s.lastIndexOf('}');

        if (l != -1 && r != -1 && r > 1) {
            return s.substring(l, r + 1);
        }

        throw new FishingException(ErrorCode.GPT_RESPONSE_PARSE_ERROR);
    }

}
