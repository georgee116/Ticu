package com.example.gatewayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/banking")
public class BankingGatewayController {

    @GetMapping
    public ResponseEntity<String> banking(){
        return ResponseEntity.ok("Hello in Banking Portal");
    }

    @GetMapping("/access-token")
    public String getAccessToken(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        OAuth2AccessToken token = client.getAccessToken();
        return token.getTokenValue();
    }

    @GetMapping("/id-token")
    public Map<String, Object> getIdToken(@AuthenticationPrincipal OidcUser oidcUser) {
        Map<String, Object> info = new HashMap<>();
        info.put("idToken", oidcUser.getIdToken().getTokenValue());
        info.put("claims", oidcUser.getClaims());
        info.put("authorities", oidcUser.getAuthorities());
        return info;
    }
}