package com.empresa.orderhub.application.port.in;

import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderItem;

import java.util.List;

/**
 * Input Port (Contrato de Entrada) para criação de novos pedidos.
 * Zero dependências de framework externo.
 */
public interface CreateOrderUseCase {

    Order execute(CreateOrderCommand command);

    record CreateOrderCommand(
            String customerId,
            String customerName,
            List<OrderItem> items
    ) {}
}
