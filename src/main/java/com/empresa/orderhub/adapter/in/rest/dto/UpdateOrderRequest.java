package com.empresa.orderhub.adapter.in.rest.dto;

import com.empresa.orderhub.domain.model.OrderStatus;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Serdeable
@Schema(name = "UpdateOrderRequest", description = "Payload de requisição para atualização integral de um pedido existente")
public record UpdateOrderRequest(
        @Schema(description = "Identificador único do cliente", example = "CUST-1001", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @Schema(description = "Nome completo do cliente", example = "Maria Silva", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerName,

        @Schema(description = "Lista atualizada de itens do pedido", requiredMode = Schema.RequiredMode.REQUIRED)
        List<OrderItemRequest> items,

        @Nullable
        @Schema(description = "Novo status opcional do pedido (respeitando máquina de estados do domínio)", example = "CONFIRMED")
        OrderStatus status
) {}
