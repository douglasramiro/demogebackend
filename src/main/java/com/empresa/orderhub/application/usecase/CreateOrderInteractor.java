package com.empresa.orderhub.application.usecase;

import com.empresa.orderhub.application.port.in.CreateOrderUseCase;
import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import com.empresa.orderhub.domain.model.Order;

import java.util.Objects;

/**
 * Implementação do Caso de Uso de Criação de Pedido.
 * Não possui anotações de framework (@Singleton/@Inject); é instanciado via @Factory na camada infrastructure.
 */
public class CreateOrderInteractor implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public CreateOrderInteractor(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository não pode ser nulo");
    }

    @Override
    public Order execute(CreateOrderCommand command) {
        if (command == null) {
            throw new OrderValidationException("Os dados de criação do pedido são obrigatórios.");
        }
        Order newOrder = Order.create(
                command.customerId(),
                command.customerName(),
                command.items()
        );
        return orderRepository.save(newOrder);
    }
}
