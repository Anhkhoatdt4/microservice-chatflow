package com.qlda.profileservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProfileSwaggerConfig {
    @Bean
    public OpenAPI swaggerConfig() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Profile Service API")
                        .version("1.0.0")
                        .description("API documentation for the Profile Service")
                ).servers(List.of(
                        new Server().url("/profile").description("Profile Service API Server")
                ));
    }
}
