package com.empresa.orderhub.domain.model;

import com.empresa.orderhub.domain.exception.InvalidOrderStateException;
import com.empresa.orderhub.domain.exception.OrderValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entidade de Domínio pura modelada com Java Record imutável.
 * Contém validações intrínsecas e regras invariantes de negócio sem anotações de framework.
 */
public record Order(
        UUID id,
        String customerId,
        String customerName,
        List<OrderItem> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public Order {
        if (id == null) {
            throw new OrderValidationException("O ID do pedido é obrigatório.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new OrderValidationException("O ID do cliente (customerId) é obrigatório.");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new OrderValidationException("O nome do cliente (customerName) é obrigatório.");
        }
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("O pedido deve conter ao menos um item.");
        }
        if (status == null) {
            throw new OrderValidationException("O status do pedido é obrigatório.");
        }
        if (createdAt == null || updatedAt == null) {
            throw new OrderValidationException("As datas de criação e atualização são obrigatórias.");
        }
        customerId = customerId.trim();
        customerName = customerName.trim();
        items = List.copyOf(items);
        totalAmount = calculateTotal(items);
    }

    public static Order create(String customerId, String customerName, List<OrderItem> items) {
        Instant now = Instant.now();
        return new Order(
                UUID.randomUUID(),
                customerId,
                customerName,
                items,
                calculateTotal(items),
                OrderStatus.PENDING,
                now,
                now
        );
    }

    public Order updateDetails(String newCustomerId, String newCustomerName, List<OrderItem> newItems, OrderStatus newStatus) {
        if (this.status.isTerminal()) {
            throw new InvalidOrderStateException(
                    "Não é possível alterar um pedido em estado terminal (" + this.status + ").",
                    this.status,
                    newStatus != null ? newStatus : this.status
            );
        }
        OrderStatus targetStatus = (newStatus != null) ? newStatus : this.status;
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStateException(
                    "Transição de status inválida de " + this.status + " para " + targetStatus + ".",
                    this.status,
                    targetStatus
            );
        }
        return new Order(
                this.id,
                newCustomerId,
                newCustomerName,
                newItems,
                calculateTotal(newItems),
                targetStatus,
                this.createdAt,
                Instant.now()
        );
    }

    public Order transitionStatus(OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new OrderValidationException("O novo status de destino é obrigatório.");
        }
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStateException(
                    "Transição de status inválida de " + this.status + " para " + targetStatus + ".",
                    this.status,
                    targetStatus
            );
        }
        return new Order(
                this.id,
                this.customerId,
                this.customerName,
                this.items,
                this.totalAmount,
                targetStatus,
                this.createdAt,
                Instant.now()
        );
    }

    private static BigDecimal calculateTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("O pedido deve conter ao menos um item.");
        }
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
