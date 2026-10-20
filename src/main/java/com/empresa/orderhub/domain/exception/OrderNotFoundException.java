package com.empresa.orderhub.domain.exception;

import java.util.UUID;

/**
 * Exceção disparada quando um pedido não é encontrado (mapeada para HTTP 404 Not Found).
 */
public final class OrderNotFoundException extends DomainException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("ORDER_NOT_FOUND", "Pedido não encontrado com o ID: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
