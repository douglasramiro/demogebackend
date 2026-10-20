package com.empresa.orderhub.adapter.in.rest;

import com.empresa.orderhub.adapter.in.rest.dto.CreateOrderRequest;
import com.empresa.orderhub.adapter.in.rest.dto.ErrorResponse;
import com.empresa.orderhub.adapter.in.rest.dto.OrderResponse;
import com.empresa.orderhub.adapter.in.rest.dto.UpdateOrderRequest;
import com.empresa.orderhub.adapter.in.rest.dto.UpdateOrderStatusRequest;
import com.empresa.orderhub.adapter.in.rest.mapper.OrderRestMapper;
import com.empresa.orderhub.application.port.in.CreateOrderUseCase;
import com.empresa.orderhub.application.port.in.DeleteOrderUseCase;
import com.empresa.orderhub.application.port.in.GetOrderUseCase;
import com.empresa.orderhub.application.port.in.UpdateOrderUseCase;
import com.empresa.orderhub.domain.exception.OrderValidationException;
import com.empresa.orderhub.domain.model.Order;
import com.empresa.orderhub.domain.model.OrderStatus;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador REST (Micronaut Controller) para operações CRUD de pedidos.
 * Executa sobre Virtual Threads (Project Loom) via TaskExecutors.BLOCKING no Java 21
 * e depende exclusivamente dos Input Ports da camada Application.
 */
@Controller("/api/v1/orders")
@ExecuteOn(TaskExecutors.BLOCKING)
@Tag(name = "Orders", description = "Endpoints CRUD para gerenciamento de pedidos em memória no OrderHub")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderUseCase updateOrderUseCase;
    private final DeleteOrderUseCase deleteOrderUseCase;
    private final OrderRestMapper mapper;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            UpdateOrderUseCase updateOrderUseCase,
            DeleteOrderUseCase deleteOrderUseCase,
            OrderRestMapper mapper
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.updateOrderUseCase = updateOrderUseCase;
        this.deleteOrderUseCase = deleteOrderUseCase;
        this.mapper = mapper;
    }

    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Status(HttpStatus.CREATED)
    @Operation(
            summary = "Criar um novo pedido",
            description = "Cria um novo pedido em memória com status inicial PENDING e calcula automaticamente os subtotais e valor total."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (violação de regra de domínio)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public HttpResponse<OrderResponse> create(@Body CreateOrderRequest request) {
        Order created = createOrderUseCase.execute(mapper.toCommand(request));
        return HttpResponse
                .created(mapper.toResponse(created))
                .headers(headers -> headers.location(URI.create("/api/v1/orders/" + created.id())));
    }

    @Get(produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Listar todos os pedidos",
            description = "Retorna a lista de todos os pedidos armazenados em memória, permitindo filtragem opcional por status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    })
    public List<OrderResponse> findAll(
            @Parameter(description = "Filtro opcional pelo status do pedido (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)")
            @Nullable @QueryValue OrderStatus status
    ) {
        return getOrderUseCase.findAll(status).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Buscar pedido por ID",
            description = "Recupera os detalhes completos de um pedido a partir do seu identificador UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse findById(
            @Parameter(description = "UUID do pedido", required = true)
            @PathVariable UUID id
    ) {
        Order order = getOrderUseCase.findById(id);
        return mapper.toResponse(order);
    }

    @Put(uri = "/{id}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Atualizar um pedido existente",
            description = "Atualiza dados do cliente, itens e opcionalmente o status de um pedido que não esteja em estado terminal (DELIVERED ou CANCELLED)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Transição de estado inválida ou pedido em estado terminal",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse update(
            @Parameter(description = "UUID do pedido", required = true)
            @PathVariable UUID id,
            @Body UpdateOrderRequest request
    ) {
        Order updated = updateOrderUseCase.update(id, mapper.toCommand(request));
        return mapper.toResponse(updated);
    }

    @Patch(uri = "/{id}/status", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Atualizar status do pedido",
            description = "Executa a transição de estado do pedido validando as regras de ciclo de vida do domínio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status do pedido atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status não informado ou inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Transição de status não permitida pelas regras de negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OrderResponse updateStatus(
            @Parameter(description = "UUID do pedido", required = true)
            @PathVariable UUID id,
            @Body UpdateOrderStatusRequest request
    ) {
        if (request == null || request.status() == null) {
            throw new OrderValidationException("O novo status do pedido é obrigatório.");
        }
        Order updated = updateOrderUseCase.updateStatus(id, request.status());
        return mapper.toResponse(updated);
    }

    @Delete(uri = "/{id}")
    @Status(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remover um pedido",
            description = "Exclui permanentemente um pedido do repositório em memória a partir do seu UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public HttpResponse<Void> delete(
            @Parameter(description = "UUID do pedido", required = true)
            @PathVariable UUID id
    ) {
        deleteOrderUseCase.deleteById(id);
        return HttpResponse.noContent();
    }
}
