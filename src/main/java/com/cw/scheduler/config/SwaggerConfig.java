package com.cw.scheduler.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url}")
    private String serverUrl;

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Appointment Scheduler - Monolithic Backend API")
                        .description("This API provides endpoints for user registration, authentication, appointment booking, provider schedules, reviews, notifications, and admin management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vishal Tiwari")
                                .email("vishal@example.com")
                                .url("https://github.com/vishaltiwari012"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                )
                .tags(List.of(
                        new Tag().name("Auth APIs").description("Authentication, login, registration, and role-based access control"),
                        new Tag().name("Admin APIs").description("Admin operations and dashboard functionality"),
                        new Tag().name("Admin-Provider APIs").description("Admin actions related to service provider approvals"),
                        new Tag().name("Appointment APIs").description("Appointment creation, update, and availability checks"),
                        new Tag().name("Category APIs").description("Service category management"),
                        new Tag().name("Individual Service APIs").description("Manage individual services under offered services"),
                        new Tag().name("Offered Service APIs").description("Manage services offered by providers"),
                        new Tag().name("Notification APIs").description("User and provider notification operations"),
                        new Tag().name("Service Provider APIs").description("Service provider registration and profile"),
                        new Tag().name("Review APIs").description("Service review and rating APIs"),
                        new Tag().name("Schedule APIs").description("Provider schedule setup and retrieval"),
                        new Tag().name("User APIs").description("User profile, details, and actions")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)))
                .servers(List.of(new Server().url(serverUrl).description("Dynamic Server")));
    }
}
