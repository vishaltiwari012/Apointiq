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
                        new Tag().name("User APIs").description("User profile and personal information management"),
                        new Tag().name("Appointment APIs").description("Book, view, cancel, and manage appointments"),
                        new Tag().name("Provider Schedule APIs").description("Service provider availability and scheduling management"),
                        new Tag().name("Review APIs").description("Review and rating system for service providers"),
                        new Tag().name("Notification APIs").description("Notifications, reminders, and communication alerts"),
                        new Tag().name("Admin APIs").description("Administrative controls over users, roles, services, and schedules")
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
