package com.empresa.orderhub.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Configuração da especificação OpenAPI 3.0 / Swagger gerada em tempo de compilação (AOT).
 */
@OpenAPIDefinition(
        info = @Info(
                title = "OrderHub API",
                version = "1.0.0",
                description = "API RESTful de gerenciamento de pedidos em memória construída com Java 21 LTS, Micronaut Framework e Clean Architecture.",
                contact = @Contact(name = "Arquitetura de Backend Corporativa", email = "arquitetura@empresa.com")
        )
)
public class OpenApiConfig {
}
