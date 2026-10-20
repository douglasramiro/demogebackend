package com.empresa.orderhub.infrastructure.config;

import com.empresa.orderhub.application.port.in.CreateOrderUseCase;
import com.empresa.orderhub.application.port.in.DeleteOrderUseCase;
import com.empresa.orderhub.application.port.in.GetOrderUseCase;
import com.empresa.orderhub.application.port.in.UpdateOrderUseCase;
import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.application.usecase.CreateOrderInteractor;
import com.empresa.orderhub.application.usecase.DeleteOrderInteractor;
import com.empresa.orderhub.application.usecase.GetOrderInteractor;
import com.empresa.orderhub.application.usecase.UpdateOrderInteractor;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

/**
 * Fábrica de configuração de Injeção de Dependências (IoC AOT) do Micronaut.
 * Instancia os Casos de Uso injetando a porta de saída OrderRepositoryPort, mantendo as camadas
 * Domain e Application 100% livres de anotações e dependências de framework.
 */
@Factory
public class UseCaseFactory {

    @Singleton
    public CreateOrderUseCase createOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
        return new CreateOrderInteractor(orderRepositoryPort);
    }

    @Singleton
    public GetOrderUseCase getOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
        return new GetOrderInteractor(orderRepositoryPort);
    }

    @Singleton
    public UpdateOrderUseCase updateOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
        return new UpdateOrderInteractor(orderRepositoryPort);
    }

    @Singleton
    public DeleteOrderUseCase deleteOrderUseCase(OrderRepositoryPort orderRepositoryPort) {
        return new DeleteOrderInteractor(orderRepositoryPort);
    }
}
