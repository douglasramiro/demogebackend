package com.empresa.orderhub.application.usecase;

import com.empresa.orderhub.application.port.in.GetOrderUseCase;
import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.domain.exception.OrderNotFoundException;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementação do Caso de Uso de Consulta de Pedidos.
 * Não possui anotações de framework (@Singleton/@Inject); é instanciado via @Factory na camada infrastructure.
 */
public class GetOrderInteractor implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderInteractor(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository não pode ser nulo");
    }

    @Override
    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> findAll(OrderStatus statusFilter) {
        if (statusFilter != null) {
            return orderRepository.findByStatus(statusFilter);
        }
        return orderRepository.findAll();
    }
}
