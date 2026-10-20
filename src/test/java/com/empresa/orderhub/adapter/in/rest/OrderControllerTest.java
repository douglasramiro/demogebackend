package com.empresa.orderhub.adapter.in.rest;

import com.empresa.orderhub.adapter.in.rest.dto.CreateOrderRequest;
import com.empresa.orderhub.adapter.in.rest.dto.OrderItemRequest;
import com.empresa.orderhub.adapter.in.rest.dto.OrderResponse;
import com.empresa.orderhub.adapter.in.rest.dto.UpdateOrderRequest;
import com.empresa.orderhub.adapter.in.rest.dto.UpdateOrderStatusRequest;
import com.empresa.orderhub.adapter.out.persistence.memory.InMemoryOrderRepository;
import com.empresa.orderhub.domain.model.OrderStatus;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class OrderControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    InMemoryOrderRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.clear();
    }

    @Test
    void shouldExecuteFullInMemoryCrudLifecycle() {
        // 1. CREATE (POST /api/v1/orders)
        CreateOrderRequest createReq = new CreateOrderRequest(
                "CUST-100",
                "Maria Oliveira",
                List.of(
                        new OrderItemRequest("SKU-10", "Cadeira Ergonômica", 1, new BigDecimal("1800.00")),
                        new OrderItemRequest("SKU-20", "Suporte Articulado", 2, new BigDecimal("250.00"))
                )
        );

        HttpResponse<OrderResponse> createResp = client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/orders", createReq),
                OrderResponse.class
        );
        assertEquals(HttpStatus.CREATED, createResp.getStatus());
        OrderResponse created = createResp.body();
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(new BigDecimal("2300.00"), created.totalAmount());
        assertEquals(OrderStatus.PENDING, created.status());

        UUID orderId = created.id();

        // 2. READ BY ID (GET /api/v1/orders/{id})
        OrderResponse fetched = client.toBlocking().retrieve(
                HttpRequest.GET("/api/v1/orders/" + orderId),
                OrderResponse.class
        );
        assertEquals(orderId, fetched.id());
        assertEquals("Maria Oliveira", fetched.customerName());

        // 3. LIST ALL (GET /api/v1/orders)
        List<OrderResponse> allOrders = client.toBlocking().retrieve(
                HttpRequest.GET("/api/v1/orders"),
                Argument.listOf(OrderResponse.class)
        );
        assertEquals(1, allOrders.size());

        // 4. UPDATE (PUT /api/v1/orders/{id})
        UpdateOrderRequest updateReq = new UpdateOrderRequest(
                "CUST-100",
                "Maria Oliveira Santos",
                List.of(new OrderItemRequest("SKU-10", "Cadeira Ergonômica", 2, new BigDecimal("1800.00"))),
                OrderStatus.CONFIRMED
        );
        OrderResponse updated = client.toBlocking().retrieve(
                HttpRequest.PUT("/api/v1/orders/" + orderId, updateReq),
                OrderResponse.class
        );
        assertEquals("Maria Oliveira Santos", updated.customerName());
        assertEquals(new BigDecimal("3600.00"), updated.totalAmount());
        assertEquals(OrderStatus.CONFIRMED, updated.status());

        // 5. PATCH STATUS (PATCH /api/v1/orders/{id}/status)
        OrderResponse shipped = client.toBlocking().retrieve(
                HttpRequest.PATCH("/api/v1/orders/" + orderId + "/status", new UpdateOrderStatusRequest(OrderStatus.SHIPPED)),
                OrderResponse.class
        );
        assertEquals(OrderStatus.SHIPPED, shipped.status());

        // 6. DELETE (DELETE /api/v1/orders/{id})
        HttpResponse<Void> deleteResp = client.toBlocking().exchange(
                HttpRequest.DELETE("/api/v1/orders/" + orderId)
        );
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatus());

        // 7. VERIFY 404 AFTER DELETE
        HttpClientResponseException notFoundEx = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().retrieve(HttpRequest.GET("/api/v1/orders/" + orderId), OrderResponse.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, notFoundEx.getStatus());
    }

    @Test
    void shouldMapDomainExceptionsToHttp400And422() {
        // 400 Bad Request para pedido sem itens
        CreateOrderRequest invalidReq = new CreateOrderRequest("CUST-1", "Teste", List.of());
        HttpClientResponseException badReqEx = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().exchange(HttpRequest.POST("/api/v1/orders", invalidReq), OrderResponse.class)
        );
        assertEquals(HttpStatus.BAD_REQUEST, badReqEx.getStatus());

        // 422 Unprocessable Entity para transição de status inválida (PENDING -> DELIVERED)
        CreateOrderRequest validReq = new CreateOrderRequest(
                "CUST-1",
                "Teste",
                List.of(new OrderItemRequest("SKU-1", "Item", 1, new BigDecimal("100.00")))
        );
        OrderResponse created = client.toBlocking().retrieve(
                HttpRequest.POST("/api/v1/orders", validReq),
                OrderResponse.class
        );

        HttpClientResponseException unprocessableEx = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().exchange(
                        HttpRequest.PATCH("/api/v1/orders/" + created.id() + "/status",
                                new UpdateOrderStatusRequest(OrderStatus.DELIVERED)),
                        OrderResponse.class
                )
        );
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, unprocessableEx.getStatus());
    }

    @Test
    void shouldExposeSwaggerOpenApiSpecAndUi() {
        String openApiYaml = client.toBlocking().retrieve(HttpRequest.GET("/swagger/orderhub-api-1.0.0.yml"));
        assertNotNull(openApiYaml);
        assertTrue(openApiYaml.contains("OrderHub API"));
        assertTrue(openApiYaml.contains("/api/v1/orders"));

        String swaggerUiHtml = client.toBlocking().retrieve(HttpRequest.GET("/swagger-ui/index.html"));
        assertNotNull(swaggerUiHtml);
        assertTrue(swaggerUiHtml.contains("swagger-ui"));
    }
}
