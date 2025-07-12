package com.qlda.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AuthSwaggerConfig {
    @Bean
    public OpenAPI swaggerConfig(){
        return new OpenAPI().info(
                new Info()
                        .title("Authentication Service API")
                        .version("1.0.0")
                        .description("API documentation for the Authentication Service of the Microservice project.")

        ).servers(List.of(
                new Server().url("/api").description("Base URL for the Authentication Service API")
        ));

    }
}
