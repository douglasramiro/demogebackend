package com.empresa.orderhub.domain.exception;

/**
 * Exceção disparada quando invariantes ou dados de entrada do domínio são inválidos
 * (mapeada para HTTP 400 Bad Request).
 */
public final class OrderValidationException extends DomainException {

    public OrderValidationException(String message) {
        super("ORDER_VALIDATION_ERROR", message);
    }
}
