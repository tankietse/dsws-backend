package com.webgis.dsws.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Disease Warning System API")
                        .version("1.0.0")
                        .description("API documentation for Disease Warning System"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("dsws-public")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(openApi -> openApi.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication")))
                .build();
    }
}