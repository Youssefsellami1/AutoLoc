package tn.esprit.autolocn.web.controller;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de SpringDoc / OpenAPI (Atelier 5, étape 10).
 * Génère la spécification OpenAPI (le contrat de l'API) et renseigne
 * Swagger UI (groupe /info) pour l'application AutoLoc.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI autolocOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AutoLoc API")
                .description("API REST de gestion de location de véhicules")
                .version("1.0.0"));
    }
}
