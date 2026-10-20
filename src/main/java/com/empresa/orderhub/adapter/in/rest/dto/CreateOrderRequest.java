package com.empresa.orderhub.adapter.in.rest.dto;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Serdeable
@Schema(name = "CreateOrderRequest", description = "Payload de requisição para criação de um novo pedido")
public record CreateOrderRequest(
        @Schema(description = "Identificador único do cliente", example = "CUST-1001", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @Schema(description = "Nome completo do cliente", example = "Maria Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerName,

        @Schema(description = "Lista de itens que compõem o pedido", requiredMode = Schema.RequiredMode.REQUIRED)
        List<OrderItemRequest> items
) {}
