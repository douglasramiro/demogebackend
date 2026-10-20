package com.empresa.orderhub.domain.exception;

import com.empresa.orderhub.domain.model.OrderStatus;

/**
 * Exceção disparada quando uma operação viola o ciclo de vida ou estado atual do pedido
 * (mapeada para HTTP 422 Unprocessable Entity).
 */
public final class InvalidOrderStateException extends DomainException {

    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public InvalidOrderStateException(String message, OrderStatus currentStatus, OrderStatus targetStatus) {
        super("INVALID_ORDER_STATE", message);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
}
