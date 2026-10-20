package com.empresa.orderhub.application.port.in;

import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderItem;
import com.empresa.orderhub.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Contrato de Entrada) para atualização integral e transição de status de pedidos.
 * Zero dependências de framework externo.
 */
public interface UpdateOrderUseCase {

    Order update(UUID id, UpdateOrderCommand command);

    Order updateStatus(UUID id, OrderStatus targetStatus);

    record UpdateOrderCommand(
            String customerId,
            String customerName,
            List<OrderItem> items,
            OrderStatus status
    ) {}
}
