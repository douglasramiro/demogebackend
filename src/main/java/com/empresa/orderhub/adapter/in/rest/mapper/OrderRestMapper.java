package com.empresa.orderhub.adapter.in.rest.mapper;

import com.empresa.orderhub.adapter.in.rest.dto.CreateOrderRequest;
import com.empresa.orderhub.adapter.in.rest.dto.OrderItemRequest;
import com.empresa.orderhub.adapter.in.rest.dto.OrderItemResponse;
import com.empresa.orderhub.adapter.in.rest.dto.OrderResponse;
import com.empresa.orderhub.adapter.in.rest.dto.UpdateOrderRequest;
import com.empresa.orderhub.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.empresa.orderhub.application.port.in.UpdateOrderUseCase.UpdateOrderCommand;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderItem;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Mapper da camada de Interface Adapters responsável por converter DTOs REST
 * em Commands/Entidades de Domínio e vice-versa.
 */
@Singleton
public class OrderRestMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        if (request == null) {
            throw new OrderValidationException("O corpo da requisição (CreateOrderRequest) é obrigatório.");
        }
        return new CreateOrderCommand(
                request.customerId(),
                request.customerName(),
                toDomainItems(request.items())
        );
    }

    public UpdateOrderCommand toCommand(UpdateOrderRequest request) {
        if (request == null) {
            throw new OrderValidationException("O corpo da requisição (UpdateOrderRequest) é obrigatório.");
        }
        return new UpdateOrderCommand(
                request.customerId(),
                request.customerName(),
                toDomainItems(request.items()),
                request.status()
        );
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.items().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.customerName(),
                itemResponses,
                order.totalAmount(),
                order.status(),
                order.createdAt(),
                order.updatedAt()
        );
    }

    private List<OrderItem> toDomainItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("O pedido deve conter ao menos um item.");
        }
        return items.stream()
                .map(item -> {
                    if (item == null) {
                        throw new OrderValidationException("Item do pedido não pode ser nulo.");
                    }
                    return new OrderItem(
                            item.productId(),
                            item.productName(),
                            item.quantity(),
                            item.unitPrice()
                    );
                })
                .toList();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.productId(),
                item.productName(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal()
        );
    }
}
