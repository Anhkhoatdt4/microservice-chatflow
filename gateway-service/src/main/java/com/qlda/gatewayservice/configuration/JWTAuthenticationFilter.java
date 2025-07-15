package com.qlda.gatewayservice.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.gatewayservice.dto.ApiResponse;
import com.qlda.gatewayservice.dto.IntrospectRequest;
import com.qlda.gatewayservice.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE , makeFinal = true)
public class JWTAuthenticationFilter implements GlobalFilter , Ordered {

    private final IdentityClient identityClient;
    private final ObjectMapper objectMapper;

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    // PUBLIC endpoints not check access token
    private final String[] PUBLIC_ENDPOINTS = {
            "/identity/auth/.*",
            "/identity/users/registration",
            "/notification/email/send",
            "/file/media/download/.*",
            "/swagger-ui.*",
            "/v3/api-docs.*"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter and check ...");
        log.info("JWTAuthenticationFilter is called for request: {}", exchange.getRequest().getURI());
        log.info("Request headers: {}", exchange.getRequest().getHeaders());

        if (isPublicEndpoint((ServerHttpRequest) exchange.getRequest())){
            return chain.filter(exchange);
        }
        // If the endpoint is not public, we can add logic to check JWT token here
        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)){
            log.warn("Authorization header is missing in the request: {}", exchange.getRequest().getURI());
            return Mono.error(new RuntimeException("Authorization header is missing"));
        }
        String token = authHeaders.get(0);
        IntrospectRequest request = IntrospectRequest.builder()
                .token(token)
                .build();
        try {
            return identityClient.introspect(request)
                    .flatMap(response -> {
                        if (response.getResult().isValid()) {
                            return chain.filter(exchange);
                        } else {
                            return unauthenticated(exchange.getResponse());
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Token introspection failed", e);
                        return unauthenticated(exchange.getResponse());
                    });
        } catch (Exception e) {
            log.error("Error during token validation", e);
            return unauthenticated(exchange.getResponse());
        }
    }

    private boolean isPublicEndpoint(ServerHttpRequest serverHttpRequest){
        String path = serverHttpRequest.getURI().getPath();
        String apiPrefix = this.apiPrefix.endsWith("/") ? this.apiPrefix : this.apiPrefix + "/";
        log.info("Original path: {}", path);
        log.info("API prefix: {}", apiPrefix);

        if (path.startsWith(apiPrefix)) {
            path = path.substring(apiPrefix.length());
        }
        log.info("Path after trimming apiPrefix: {}", path);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        for (String endpoint : PUBLIC_ENDPOINTS) {
            log.info("Checking path '{}' against public pattern '{}'", path, endpoint);
            if (path.matches(endpoint)) {
                log.info("Matched public endpoint: {}", endpoint);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticated(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
