package com.empresa.orderhub.adapter.in.rest.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Serdeable
@Schema(name = "ErrorResponse", description = "Estrutura padronizada de erro corporativo retornada pela API")
public record ErrorResponse(
        @Schema(description = "Código HTTP do erro", example = "400")
        int status,

        @Schema(description = "Código semântico de negócio", example = "ORDER_VALIDATION_ERROR")
        String errorCode,

        @Schema(description = "Mensagem explicativa do erro", example = "O pedido deve conter ao menos um item.")
        String message,

        @Schema(description = "Caminho URI da requisição", example = "/api/v1/orders")
        String path,

        @Schema(description = "Timestamp UTC da ocorrência")
        Instant timestamp
) {}
