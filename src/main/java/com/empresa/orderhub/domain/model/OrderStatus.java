package com.empresa.orderhub.domain.model;

/**
 * Estados possíveis de um pedido no ciclo de vida do domínio.
 * Utiliza Pattern Matching em switch (Java 21) para validar transições permitidas.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true;
        }
        return switch (this) {
            case PENDING -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == SHIPPED || target == CANCELLED;
            case SHIPPED -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return switch (this) {
            case DELIVERED, CANCELLED -> true;
            case PENDING, CONFIRMED, SHIPPED -> false;
        };
    }
}
