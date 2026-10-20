package com.empresa.orderhub.application.port.in;

import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Contrato de Entrada) para consulta de pedidos.
 * Zero dependências de framework externo.
 */
public interface GetOrderUseCase {

    Order findById(UUID id);

    List<Order> findAll(OrderStatus statusFilter);
}
