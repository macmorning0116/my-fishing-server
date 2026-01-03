package com.yechan.fishing.fishing_api.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Component
@Slf4j
public class WebClientLoggingFilter {

    public ExchangeFilterFunction externalApiLoggingFilter(String clientName) {
        return ((request, next) -> {
            long start = System.currentTimeMillis();

            return next.exchange(request)
                    .doOnSuccess(response -> {
                        long duration = System.currentTimeMillis() - start;
                        log.info(
                                "{} API call success method={} uri={} status={} duration={}ms",
                                clientName,
                                request.method(),
                                request.url().getPath(),
                                response.statusCode(),
                                duration
                        );
                    })
                    .doOnError(e -> {
                        long duration = System.currentTimeMillis() - start;
                        log.error(
                                "{} API call failed method={} uri={} duration={}ms",
                                clientName,
                                request.method(),
                                request.url().getPath(),
                                duration,
                                e
                        );
                    });
        });
    }
}
