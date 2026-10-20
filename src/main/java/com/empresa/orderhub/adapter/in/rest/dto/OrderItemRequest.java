package com.empresa.orderhub.adapter.in.rest.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Serdeable
@Schema(name = "OrderItemRequest", description = "Dados de um item do pedido na requisição")
public record OrderItemRequest(
        @Schema(description = "Identificador único do produto", example = "PROD-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String productId,

        @Schema(description = "Nome descritivo do produto", example = "Teclado Mecânico Sem Fio", requiredMode = Schema.RequiredMode.REQUIRED)
        String productName,

        @Schema(description = "Quantidade comprada do produto (deve ser > 0)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity,

        @Schema(description = "Preço unitário do produto (deve ser > 0)", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal unitPrice
) {}
