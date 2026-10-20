package com.empresa.orderhub.domain.exception;

/**
 * Hierarquia selada (Java 21 Sealed Class) para exceções de negócio do domínio.
 * Permite Pattern Matching exaustivo no ExceptionHandler global sem acoplar o domínio a HTTP.
 */
public abstract sealed class DomainException extends RuntimeException
        permits OrderNotFoundException, OrderValidationException, InvalidOrderStateException {

    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
