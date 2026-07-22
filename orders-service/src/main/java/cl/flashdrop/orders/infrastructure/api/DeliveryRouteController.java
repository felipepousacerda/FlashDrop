package cl.flashdrop.orders.infrastructure.api;

import cl.flashdrop.orders.application.usecase.ClaimDeliveryOrdersUseCase;
import cl.flashdrop.orders.application.usecase.ListDeliveryRoutesUseCase;
import cl.flashdrop.orders.domain.model.DeliveryRoute;
import cl.flashdrop.orders.domain.model.Order;
import cl.flashdrop.orders.infrastructure.api.dto.request.ClaimDeliveryRequest;
import cl.flashdrop.orders.infrastructure.api.dto.response.ApiResponse;
import cl.flashdrop.orders.infrastructure.api.dto.response.DeliveryRouteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para el dominio logístico de Reparto.
 *
 * Endpoints conforme al contrato openapi.yaml:
 * - GET  /api/delivery/routes  → retorna array plano de DeliveryRouteResponse
 * - POST /api/delivery/claim   → acepta {deliveryPersonId, orderId} y retorna ApiResponse<DeliveryRouteResponse>
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final ListDeliveryRoutesUseCase listRoutesUseCase;
    private final ClaimDeliveryOrdersUseCase claimOrdersUseCase;

    /**
     * Lista rutas de delivery disponibles.
     * Retorna array JSON plano según contrato openapi.yaml (sin wrapper ApiResponse).
     */
    @GetMapping("/routes")
    public List<DeliveryRouteResponse> listRoutes() {
        log.debug("GET /api/delivery/routes");
        List<Order> orders = listRoutesUseCase.execute();
        return orders.stream()
                .map(this::toRouteResponse)
                .collect(Collectors.toList());
    }

    /**
     * Un repartidor reclama un pedido para entrega.
     *
     * Acepta formato openapi.yaml: {@code { "deliveryPersonId": 1, "orderId": 5 }}
     * También acepta formato legacy: {@code { "user_id": 1, "order_ids": [5] }}
     *
     * Retorna ApiResponse<DeliveryRouteResponse> con los datos de la ruta asignada.
     */
    @PostMapping("/claim")
    public ApiResponse<DeliveryRouteResponse> claimDeliveryOrder(@Valid @RequestBody ClaimDeliveryRequest request) {
        List<UUID> resolvedOrderIds = request.resolvedOrderIds();
        UUID userId = request.getUserId();

        log.debug("POST /api/delivery/claim, deliveryPersonId={}, orderIds={}", userId, resolvedOrderIds);

        if (resolvedOrderIds.isEmpty()) {
            throw new cl.flashdrop.orders.domain.exception.OrderDomainException(
                "Debes seleccionar al menos un pedido"
            );
        }

        claimOrdersUseCase.execute(userId, resolvedOrderIds);

        // Recuperar la ruta del primer pedido reclamado para retornarla en la respuesta
        UUID firstOrderId = resolvedOrderIds.get(0);
        List<Order> allRoutes = listRoutesUseCase.execute();
        Order claimedOrder = allRoutes.stream()
                .filter(o -> firstOrderId.equals(o.getId()))
                .findFirst()
                .orElse(null);

        DeliveryRouteResponse routeResponse = claimedOrder != null
                ? toRouteResponse(claimedOrder)
                : DeliveryRouteResponse.builder()
                        .orderId(firstOrderId)
                        .status("En camino")
                        .build();

        return ApiResponse.success("Pedidos tomados. Tu ruta esta activa", routeResponse);
    }

    // =========================================================================
    // Mappers
    // =========================================================================

    private DeliveryRouteResponse toRouteResponse(Order order) {
        DeliveryRoute route = order.getRoute();

        return DeliveryRouteResponse.builder()
                .id(route != null ? route.getId() : null)
                .orderId(order.getId())
                .pickupAddress(route != null ? route.getPickupAddress() : "")
                .deliveryAddress(route != null ? route.getDeliveryAddress() : "")
                .distanceKm(route != null ? route.getDistanceKm() : BigDecimal.ZERO)
                .estimatedMinutes(route != null ? route.getEstimatedMinutes() : 0)
                .status(order.getStatus().getValue())
                .build();
    }
}

