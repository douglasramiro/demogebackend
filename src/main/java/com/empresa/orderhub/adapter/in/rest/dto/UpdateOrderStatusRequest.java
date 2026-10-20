package com.empresa.orderhub.adapter.in.rest.dto;

import com.empresa.orderhub.domain.model.OrderStatus;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
@Schema(name = "UpdateOrderStatusRequest", description = "Payload para transição de status do pedido")
public record UpdateOrderStatusRequest(
        @Schema(description = "Status de destino para o pedido", example = "CONFIRMED", requiredMode = Schema.RequiredMode.REQUIRED)
        OrderStatus status
) {}
