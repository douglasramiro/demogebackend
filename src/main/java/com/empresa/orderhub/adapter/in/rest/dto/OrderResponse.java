package com.empresa.orderhub.adapter.in.rest.dto;

import com.empresa.orderhub.domain.model.OrderStatus;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
@Schema(name = "OrderResponse", description = "Representação completa de um pedido retornado pela API")
public record OrderResponse(
        @Schema(description = "UUID único do pedido", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,

        @Schema(description = "Identificador do cliente", example = "CUST-1001")
        String customerId,

        @Schema(description = "Nome do cliente", example = "Maria Silva")
        String customerName,

        @Schema(description = "Itens do pedido")
        List<OrderItemResponse> items,

        @Schema(description = "Valor total calculado do pedido", example = "900.00")
        BigDecimal totalAmount,

        @Schema(description = "Status atual do pedido", example = "PENDING")
        OrderStatus status,

        @Schema(description = "Timestamp UTC de criação do pedido")
        Instant createdAt,

        @Schema(description = "Timestamp UTC da última atualização do pedido")
        Instant updatedAt
) {}
