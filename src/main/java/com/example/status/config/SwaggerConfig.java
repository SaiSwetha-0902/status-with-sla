package com.example.status.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI statusTrackingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Status Tracking Microservice API")
                        .description("API for tracking order status with SLA monitoring")
                        .version("1.0.0"));
    }
}