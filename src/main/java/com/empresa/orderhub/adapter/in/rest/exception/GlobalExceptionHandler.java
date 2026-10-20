package com.empresa.orderhub.adapter.in.rest.exception;

import com.empresa.orderhub.adapter.in.rest.dto.ErrorResponse;
import com.empresa.orderhub.domain.exception.DomainException;
import com.empresa.orderhub.domain.exception.InvalidOrderStateException;
import com.empresa.orderhub.domain.exception.OrderNotFoundException;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import java.time.Instant;

/**
 * ExceptionHandler global do Micronaut para tratamento de exceções corporativas do domínio.
 * Utiliza Pattern Matching exaustivo sobre Sealed Classes (Java 21) para converter
 * exceções de domínio em respostas HTTP 400, 404 e 422.
 */
@Produces
@Singleton
@Requires(classes = {DomainException.class, ExceptionHandler.class})
public class GlobalExceptionHandler implements ExceptionHandler<DomainException, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(HttpRequest request, DomainException exception) {
        HttpStatus status = switch (exception) {
            case OrderValidationException ignored -> HttpStatus.BAD_REQUEST;
            case OrderNotFoundException ignored -> HttpStatus.NOT_FOUND;
            case InvalidOrderStateException ignored -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        ErrorResponse body = new ErrorResponse(
                status.getCode(),
                exception.getErrorCode(),
                exception.getMessage(),
                request.getPath(),
                Instant.now()
        );

        return HttpResponse.status(status).body(body);
    }
}
