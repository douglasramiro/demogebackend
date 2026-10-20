package com.empresa.orderhub.adapter.out.persistence.memory;

import com.empresa.orderhub.application.port.out.OrderRepositoryPort;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de Persistência em Memória que implementa o Output Port OrderRepositoryPort.
 * Thread-safe utilizando ConcurrentHashMap para operações concorrentes com Virtual Threads.
 */
@Singleton
public class InMemoryOrderRepository implements OrderRepositoryPort {

    private final Map<UUID, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.id(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Order::createdAt))
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return storage.values().stream()
                .filter(order -> order.status() == status)
                .sorted(Comparator.comparing(Order::createdAt))
                .toList();
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }

    public void clear() {
        storage.clear();
    }
}
