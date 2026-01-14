package com.example.gatewayserver.filters.customizable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class ContentLanguageFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(ContentLanguageFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String acceptLanguage = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        String detectedLanguage = detectLanguage(acceptLanguage);

        logger.info("Content-Language filter: Detected language = {}", detectedLanguage);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Content-Language", detectedLanguage)
                        .header("X-Client-Region", getRegionFromLanguage(detectedLanguage))
                        .build())
                .build();

        // Answ header
        modifiedExchange.getResponse()
                .getHeaders()
                .add("Content-Language", detectedLanguage);
        modifiedExchange.getResponse()
                .getHeaders()
                .add("X-Content-Language", detectedLanguage);
        modifiedExchange.getResponse()
                .getHeaders()
                .add("X-Client-Region", getRegionFromLanguage(detectedLanguage));

        return chain.filter(modifiedExchange);
    }

    private String detectLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "ro-RO";
        }

        if (acceptLanguage.contains("en")) {
            return "en-US";
        } else if (acceptLanguage.contains("ro")) {
            return "ro-RO";
        } else if (acceptLanguage.contains("de")) {
            return "de-DE";
        } else if (acceptLanguage.contains("fr")) {
            return "fr-FR";
        }

        return "ro-RO";
    }

    private String getRegionFromLanguage(String language) {
        switch (language) {
            case "en-US":
                return "US";
            case "ro-RO":
                return "RO";
            case "de-DE":
                return "DE";
            case "fr-FR":
                return "FR";
            default:
                return "RO";
        }
    }
}