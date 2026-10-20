package com.empresa.orderhub.application.usecase;

import com.empresa.orderhub.application.port.in.UpdateOrderUseCase;
import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.domain.exception.OrderNotFoundException;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação do Caso de Uso de Atualização de Pedido.
 * Não possui anotações de framework (@Singleton/@Inject); é instanciado via @Factory na camada infrastructure.
 */
public class UpdateOrderInteractor implements UpdateOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public UpdateOrderInteractor(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository não pode ser nulo");
    }

    @Override
    public Order update(UUID id, UpdateOrderCommand command) {
        if (command == null) {
            throw new OrderValidationException("Os dados de atualização do pedido são obrigatórios.");
        }
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Order updatedOrder = existingOrder.updateDetails(
                command.customerId(),
                command.customerName(),
                command.items(),
                command.status()
        );
        return orderRepository.save(updatedOrder);
    }

    @Override
    public Order updateStatus(UUID id, OrderStatus targetStatus) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Order updatedOrder = existingOrder.transitionStatus(targetStatus);
        return orderRepository.save(updatedOrder);
    }
}
