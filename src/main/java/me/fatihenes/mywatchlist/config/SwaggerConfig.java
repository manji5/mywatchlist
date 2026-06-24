package me.fatihenes.mywatchlist.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MyWatchList API", version = "1.0",
                description = "Media Tracking and Management System"),
        security = {@SecurityRequirement(name = "Bearer Authentication")})
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "JWT")
public class SwaggerConfig {

}
