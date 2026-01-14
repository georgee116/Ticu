package com.example.gatewayserver.filters.customizable;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class NotificationRequestSizeFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        if (!path.startsWith("/banking/notifications")) {
            return chain.filter(exchange);
        }

        long contentLength = request.getHeaders().getContentLength();
        long requestSize = contentLength > 0 ? contentLength : 0;

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("X-Request-Size-Bytes", String.valueOf(requestSize));

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}