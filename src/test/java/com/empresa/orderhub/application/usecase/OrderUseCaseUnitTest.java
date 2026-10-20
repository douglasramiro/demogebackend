package com.empresa.orderhub.application.usecase;

import com.empresa.orderhub.adapter.out.persistence.memory.InMemoryOrderRepository;
import com.empresa.orderhub.application.port.in.CreateOrderUseCase;
import com.empresa.orderhub.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.empresa.orderhub.application.port.in.DeleteOrderUseCase;
import com.empresa.orderhub.application.port.in.GetOrderUseCase;
import com.empresa.orderhub.application.port.in.UpdateOrderUseCase;
import com.empresa.orderhub.application.port.in.UpdateOrderUseCase.UpdateOrderCommand;
import com.empresa.orderhub.domain.exception.InvalidOrderStateException;
import com.empresa.orderhub.domain.exception.OrderNotFoundException;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderItem;
import com.empresa.orderhub.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes unitários puros das camadas Domain e Application.
 * Executam sem inicializar o servidor Micronaut, comprovando o desacoplamento da Clean Architecture.
 */
class OrderUseCaseUnitTest {

    private InMemoryOrderRepository repository;
    private CreateOrderUseCase createOrderUseCase;
    private GetOrderUseCase getOrderUseCase;
    private UpdateOrderUseCase updateOrderUseCase;
    private DeleteOrderUseCase deleteOrderUseCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
        createOrderUseCase = new CreateOrderInteractor(repository);
        getOrderUseCase = new GetOrderInteractor(repository);
        updateOrderUseCase = new UpdateOrderInteractor(repository);
        deleteOrderUseCase = new DeleteOrderInteractor(repository);
    }

    @Test
    void shouldCreateAndCalculateOrderTotalCorrectly() {
        var items = List.of(
                new OrderItem("PROD-1", "Notebook Pro", 2, new BigDecimal("3500.00")),
                new OrderItem("PROD-2", "Mouse Sem Fio", 1, new BigDecimal("250.50"))
        );

        Order created = createOrderUseCase.execute(
                new CreateOrderCommand("CUST-01", "João Mendes", items)
        );

        assertNotNull(created.id());
        assertEquals(OrderStatus.PENDING, created.status());
        assertEquals(new BigDecimal("7250.50"), created.totalAmount());
        assertEquals(2, created.items().size());
    }

    @Test
    void shouldRejectOrderWithEmptyItems() {
        assertThrows(OrderValidationException.class, () ->
                createOrderUseCase.execute(new CreateOrderCommand("CUST-01", "João Mendes", List.of()))
        );
    }

    @Test
    void shouldEnforceStateTransitionRules() {
        var items = List.of(new OrderItem("PROD-1", "Monitor 4K", 1, new BigDecimal("2100.00")));
        Order created = createOrderUseCase.execute(new CreateOrderCommand("CUST-02", "Ana Costa", items));

        // Transição inválida: PENDING -> DELIVERED diretamente
        assertThrows(InvalidOrderStateException.class, () ->
                updateOrderUseCase.updateStatus(created.id(), OrderStatus.DELIVERED)
        );

        // Transições válidas: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
        Order confirmed = updateOrderUseCase.updateStatus(created.id(), OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, confirmed.status());

        Order shipped = updateOrderUseCase.updateStatus(created.id(), OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, shipped.status());

        Order delivered = updateOrderUseCase.updateStatus(created.id(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, delivered.status());

        // Pedido em estado terminal (DELIVERED) não pode mais ser editado
        assertThrows(InvalidOrderStateException.class, () ->
                updateOrderUseCase.update(created.id(), new UpdateOrderCommand("CUST-02", "Ana Costa", items, OrderStatus.DELIVERED))
        );
    }

    @Test
    void shouldDeleteExistingOrderAndThrowWhenMissing() {
        var items = List.of(new OrderItem("PROD-1", "Headset", 1, new BigDecimal("500.00")));
        Order created = createOrderUseCase.execute(new CreateOrderCommand("CUST-03", "Carlos Lima", items));

        deleteOrderUseCase.deleteById(created.id());

        assertThrows(OrderNotFoundException.class, () -> getOrderUseCase.findById(created.id()));
        assertThrows(OrderNotFoundException.class, () -> deleteOrderUseCase.deleteById(UUID.randomUUID()));
    }
}
