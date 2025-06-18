package com.sunelection.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration de la documentation OpenAPI/Swagger.
 * Cette classe configure la documentation automatique de l'API REST.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Configure l'API OpenAPI avec les informations de base et la sécurité.
     *
     * @return Une instance configurée de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("SunuElection API")
                .description(
                    "API REST pour le système de vote électronique SunuElection.\n\n" +
                    "## Fonctionnalités\n" +
                    "- Authentification sécurisée\n" +
                    "- Gestion des votes\n" +
                    "- Administration des utilisateurs\n" +
                    "- Journal d'audit\n" +
                    "- Sauvegarde et restauration\n\n" +
                    "## Sécurité\n" +
                    "L'API utilise JWT pour l'authentification. Incluez le token dans l'en-tête Authorization :\n" +
                    "```\n" +
                    "Authorization: Bearer <votre_token>\n" +
                    "```"
                )
                .version("1.0.0")
                .contact(new Contact()
                    .name("Ouleymatou Sadiya CISSE")
                    .email("ouleymatousadiyacisse@esp.sn")
                    .url("https://github.com/Ouleymatou14/SunuElection"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Serveur de développement"),
                new Server()
                    .url("https://api.sunelection.com")
                    .description("Serveur de production")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token d'authentification")));
    }
} 