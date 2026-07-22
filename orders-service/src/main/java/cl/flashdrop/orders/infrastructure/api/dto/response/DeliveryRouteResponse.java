package cl.flashdrop.orders.infrastructure.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta de rutas de reparto activas.
 */
@Getter
@Builder
public class DeliveryRouteResponse {

    private final UUID id;
    private final UUID orderId;
    private final String code;
    private final String pickupAddress;
    private final String deliveryAddress;
    private final BigDecimal distanceKm;
    private final Integer estimatedMinutes;
    private final String status;
    private final String restaurantName;
    private final UUID deliveryId;
    private final UUID deliveryUserId;
    private final String deliveryName;
    private final String deliveryPhone;
    private final String clientName;
    private final String clientPhone;
    private final BigDecimal total;
    private final String paymentMethod;
    private final int itemCount;
    private final List<String> productNames;
    private final OffsetDateTime createdAt;
}
