package com.example.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator bankingRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/banking/accounts/**")
                        .filters(f -> f
                                .addRequestHeader("X-Service", "account-service")
                                .addResponseHeader("X-Service", "account-service")
                                .rewritePath("/banking/accounts/(?<segment>.*)", "/api/accounts/${segment}")
                        )
                        .uri("lb://accountmanagement"))


                .route(p -> p
                        .path("/banking/notifications/**")
                        .filters(f -> f
                                .addRequestHeader("X-Service", "notification-service")
                                .addResponseHeader("X-Service", "notification-service")
                                .rewritePath("/banking/notifications/(?<segment>.*)", "/api/notifications/${segment}")
                        )
                        .uri("lb://notification"))


                .route(p -> p
                        .path("/banking/transactions/**")
                        .filters(f -> f
                                .addRequestHeader("X-Service", "transaction-service")
                                .addResponseHeader("X-Service", "transaction-service")
                                .rewritePath("/banking/transactions/(?<segment>.*)", "/api/transactions/${segment}")
                        )
                        .uri("lb://transactions"))

                .build();
    }
}