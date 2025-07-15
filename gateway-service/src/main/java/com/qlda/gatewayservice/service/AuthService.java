package com.qlda.gatewayservice.service;

import com.qlda.gatewayservice.dto.ApiResponse;
import com.qlda.gatewayservice.dto.IntrospectRequest;
import com.qlda.gatewayservice.dto.IntrospectResponse;
import com.qlda.gatewayservice.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    IdentityClient identityClient;

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token){
        return identityClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }
}
