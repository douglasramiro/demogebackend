package com.empresa.orderhub.adapter.in.rest.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Serdeable
@Schema(name = "OrderItemResponse", description = "Dados detalhados de um item do pedido com subtotal calculado")
public record OrderItemResponse(
        @Schema(description = "Identificador único do produto", example = "PROD-001")
        String productId,

        @Schema(description = "Nome descritivo do produto", example = "Teclado Mecânico Sem Fio")
        String productName,

        @Schema(description = "Quantidade do item", example = "2")
        int quantity,

        @Schema(description = "Preço unitário do item", example = "450.00")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal calculado do item (quantity * unitPrice)", example = "900.00")
        BigDecimal subtotal
) {}
