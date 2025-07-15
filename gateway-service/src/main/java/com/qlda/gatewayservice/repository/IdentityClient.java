package com.qlda.gatewayservice.repository;

import com.qlda.gatewayservice.dto.ApiResponse;
import com.qlda.gatewayservice.dto.IntrospectRequest;
import com.qlda.gatewayservice.dto.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
