package com.empresa.orderhub.application.usecase;

import com.empresa.orderhub.application.port.in.DeleteOrderUseCase;
import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.domain.exception.OrderNotFoundException;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação do Caso de Uso de Remoção de Pedido.
 * Não possui anotações de framework (@Singleton/@Inject); é instanciado via @Factory na camada infrastructure.
 */
public class DeleteOrderInteractor implements DeleteOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public DeleteOrderInteractor(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository não pode ser nulo");
    }

    @Override
    public void deleteById(UUID id) {
        boolean removed = orderRepository.deleteById(id);
        if (!removed) {
            throw new OrderNotFoundException(id);
        }
    }
}
