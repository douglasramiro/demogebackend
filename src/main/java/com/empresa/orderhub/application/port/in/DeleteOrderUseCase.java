package com.empresa.orderhub.application.port.in;

import java.util.UUID;

/**
 * Input Port (Contrato de Entrada) para remoção de pedidos.
 * Zero dependências de framework externo.
 */
public interface DeleteOrderUseCase {

    void deleteById(UUID id);
}
