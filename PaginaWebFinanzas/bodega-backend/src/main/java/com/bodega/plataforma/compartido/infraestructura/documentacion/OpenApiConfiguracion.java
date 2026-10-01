package com.bodega.plataforma.compartido.infraestructura.documentacion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguracion {

    @Bean
    public OpenAPI openApi() {
        var esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Cuaderno de Fiado Digital - API")
                        .description("Control de créditos de bodegas y pequeños comercios (SI642 Finanzas e Ingeniería Económica)")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().name(esquema).type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }
}
