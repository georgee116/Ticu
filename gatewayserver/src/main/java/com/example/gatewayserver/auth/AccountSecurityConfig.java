package com.example.gatewayserver.auth;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Binding;
import com.google.api.services.cloudresourcemanager.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.model.Policy;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;
import java.util.Date;
import com.google.auth.oauth2.AccessToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AccountSecurityConfig {
    private final String idProject = "tw-lau";

    @Bean
    @Profile("test")
    public SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }


    @Bean
    @Profile("!test")
    public SecurityWebFilterChain mainSecurityFilterChain(ServerHttpSecurity http) {http
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(successHandler()))
                .oauth2Client(Customizer.withDefaults())
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(HttpMethod.POST, "/banking/accounts/create_account").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/accounts/check_balance").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.PUT, "/banking/accounts/update_account_details").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/accounts/fetch_general_data").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/accounts/history").hasAnyRole("ADMIN", "CUSTOMER")


                        .pathMatchers(HttpMethod.DELETE, "/banking/accounts/close_account").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/banking/accounts/block_account").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/banking/accounts/unblock_account").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/banking/accounts/verify").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/banking/accounts/filter_by_status").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/banking/accounts/sort_by").hasRole("ADMIN")


                        .pathMatchers(HttpMethod.POST, "/banking/transactions/post").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.PUT, "/banking/transactions/put/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.DELETE, "/banking/transactions/close/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/transactions/history").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.PATCH, "/banking/transactions/complete-payment/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/transactions/calculate-fees/**").hasAnyRole("ADMIN", "CUSTOMER")

                        .pathMatchers(HttpMethod.GET, "/banking/transactions/get/**").hasAnyRole("CUSTOMER","ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/banking/transactions/modify-currency/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/banking/transactions/anti-fraud-check/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/banking/transactions/modify-transaction-type/**").hasRole("ADMIN")


                        .pathMatchers(HttpMethod.POST, "/banking/notifications/create").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/notifications/get/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.PUT, "/banking/notifications/update").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.POST, "/banking/notifications/schedule").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.POST, "/banking/notifications/send-sms/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.POST, "/banking/notifications/send-email/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.PATCH, "/banking/notifications/mark-read/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/notifications/status/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .pathMatchers(HttpMethod.GET, "/banking/notifications/history/**").hasAnyRole("ADMIN", "CUSTOMER")

                        .pathMatchers(HttpMethod.DELETE, "/banking/notifications/delete-expired").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/banking/notifications/resend-failed/**").hasRole("ADMIN")

                        .anyExchange().authenticated())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler successHandler() {
        return (webFilterExchange, authentication) -> {
            System.out.println("Authenticated authorities at success: " + authentication.getAuthorities());

            webFilterExchange
                    .getExchange()
                    .getResponse()
                    .setStatusCode(org.springframework.http.HttpStatus.FOUND);

            webFilterExchange
                    .getExchange()
                    .getResponse()
                    .getHeaders().set("Location", "http://localhost:8072/banking");

            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

        return new ReactiveOAuth2UserService<OidcUserRequest, OidcUser>() {
            @Override
            public Mono<OidcUser> loadUser(OidcUserRequest userRequest) {
                return delegate.loadUser(userRequest)
                        .map(oidcUser -> {
                            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());

                            try {
                                Set<GrantedAuthority> iamRoles = getIamRoles(userRequest, oidcUser);
                                mappedAuthorities.addAll(iamRoles);

                            }catch (GeneralSecurityException | IOException e) {
                                System.out.println(e.getMessage());
                            }

                            System.out.println("Mapped authorities: " + mappedAuthorities);

                            return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
                        });
            }
        };
    }


    private Set<GrantedAuthority> getIamRoles(OidcUserRequest userRequest, OidcUser oidcUser) throws GeneralSecurityException, IOException {

        String accessTokenValue = userRequest.getAccessToken().getTokenValue();
        System.out.println("The token's value: " + accessTokenValue);

        AccessToken accessToken = new AccessToken(accessTokenValue, Date.from(userRequest.getAccessToken().getExpiresAt()));
        System.out.println("The access token: " + accessToken.getTokenValue());

        GoogleCredentials credentials = GoogleCredentials.create(accessToken);
        System.out.println("Credentials: " + credentials);

        CloudResourceManager handler = new CloudResourceManager.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("GatewayServer")
                .build();

        GetIamPolicyRequest policyRequest = new GetIamPolicyRequest();
        Policy policy =  handler.projects().getIamPolicy(idProject, policyRequest).execute();
        System.out.println("Policy: " + policy);

        String email = oidcUser.getEmail();
        String identifier = "user:" + email;

        return policy.getBindings().stream()
                .filter(binding -> binding.getMembers() != null && binding.getMembers().contains(identifier))
                .map(Binding::getRole)
                .peek(role -> System.out.println("Role is: " + role))
                .map(this::mapIamRolesToApplicationRoles)
                .collect(Collectors.toSet());

    }

    private GrantedAuthority mapIamRolesToApplicationRoles(String role) {
        if("roles/owner".equals(role))
            return new SimpleGrantedAuthority("ROLE_ADMIN");

        if("roles/viewer".equals(role))
            return new SimpleGrantedAuthority("ROLE_CUSTOMER");

        if("roles/editor".equals(role))
            return new SimpleGrantedAuthority("ROLE_ADMIN");

        return new SimpleGrantedAuthority("ROLE_CUSTOMER");
    }
}