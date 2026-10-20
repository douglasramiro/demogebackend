package com.empresa.orderhub.domain.model;

import com.empresa.orderhub.domain.exception.OrderValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object imutável (Java Record) representando um item de pedido.
 * Zero dependências de framework externo.
 */
public record OrderItem(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new OrderValidationException("O ID do produto (productId) é obrigatório.");
        }
        if (productName == null || productName.isBlank()) {
            throw new OrderValidationException("O nome do produto (productName) é obrigatório.");
        }
        if (quantity <= 0) {
            throw new OrderValidationException("A quantidade do item deve ser maior que zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderValidationException("O preço unitário (unitPrice) deve ser maior que zero.");
        }
        productId = productId.trim();
        productName = productName.trim();
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
