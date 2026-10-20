package com.empresa.orderhub.application.port.out;

import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output Port (Gateway de Persistência) requerido pela camada de Aplicação.
 * Implementado na camada de Adapters (Inversão de Dependência - DIP).
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    boolean deleteById(UUID id);
}
