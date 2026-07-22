package cl.flashdrop.orders.infrastructure.persistence.mapper;

import cl.flashdrop.orders.domain.model.*;
import cl.flashdrop.orders.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper entre entidades JPA y modelos de dominio.
 * Centraliza todas las conversiones para mantener el dominio limpio.
 */
@Component
public class OrderMapper {

    /**
     * Convierte una OrderEntity a un Order de dominio (sólo cabecera + relaciones básicas).
     * Usado para listados donde no se necesita detalle completo.
     */
    public Order toDomain(OrderEntity entity) {
        return buildBaseOrder(entity)
                .items(Collections.emptyList())
                .route(null)
                .build();
    }

    /**
     * Convierte una OrderEntity a un Order de dominio con todos los detalles
     * (ítems, ruta, info de cliente, info de delivery).
     */
    public Order toDomainWithDetail(OrderEntity entity) {
        List<OrderItem> items = entity.getItems() == null ? Collections.emptyList()
                : entity.getItems().stream().map(this::toOrderItem).collect(Collectors.toList());

        DeliveryRoute route = entity.getRoute() != null ? toDeliveryRoute(entity.getRoute()) : null;

        return buildBaseOrder(entity)
                .items(items)
                .route(route)
                .build();
    }

    /**
     * Convierte un Order con su ruta de entrega desde la perspectiva de la ruta.
     * Usado por {@code findAllRoutesWithOrders}.
     */
    public Order toDomainWithRoute(OrderEntity entity, DeliveryRouteEntity routeEntity) {
        List<OrderItem> items = entity.getItems() == null ? Collections.emptyList()
                : entity.getItems().stream().map(this::toOrderItem).collect(Collectors.toList());

        DeliveryRoute route = toDeliveryRoute(routeEntity);

        return buildBaseOrder(entity)
                .items(items)
                .route(route)
                .build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Order.OrderBuilder buildBaseOrder(OrderEntity entity) {
        ClientInfo clientInfo = null;
        if (entity.getClient() != null) {
            UserEntity user = entity.getClient().getUser();
            clientInfo = ClientInfo.builder()
                    .clientId(entity.getClient().getId())
                    .userId(user != null ? user.getId() : null)
                    .name(user != null ? user.getName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .email(user != null ? user.getEmail() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .build();
        }

        RestaurantInfo restaurantInfo = null;
        if (entity.getRestaurant() != null) {
            restaurantInfo = RestaurantInfo.builder()
                    .restaurantId(entity.getRestaurant().getId())
                    .name(entity.getRestaurant().getName())
                    .address(entity.getRestaurant().getAddress())
                    .build();
        }

        DeliveryInfo deliveryInfo = null;
        if (entity.getDelivery() != null) {
            UserEntity user = entity.getDelivery().getUser();
            deliveryInfo = DeliveryInfo.builder()
                    .deliveryId(entity.getDelivery().getId())
                    .userId(user != null ? user.getId() : null)
                    .name(user != null ? user.getName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .vehicle(entity.getDelivery().getVehicle())
                    .build();
        }

        return Order.builder()
                .id(entity.getId())
                .clientId(entity.getClient() != null ? entity.getClient().getId() : null)
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .deliveryId(entity.getDelivery() != null ? entity.getDelivery().getId() : null)
                .status(OrderStatus.fromValue(entity.getStatus()))
                .address(entity.getAddress())
                .subtotal(entity.getSubtotal())
                .deliveryFee(entity.getDeliveryFee())
                .total(entity.getTotal())
                .paymentMethod(PaymentMethod.fromValue(entity.getPaymentMethod()))
                .createdAt(entity.getCreatedAt())
                .clientInfo(clientInfo)
                .restaurantInfo(restaurantInfo)
                .deliveryInfo(deliveryInfo);
    }

    private OrderItem toOrderItem(OrderItemEntity entity) {
        ProductEntity product = entity.getProduct();
        return OrderItem.builder()
                .id(entity.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : "Producto")
                .productDescription(product != null ? product.getDescription() : null)
                .productImage(product != null ? product.getImage() : null)
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .lineTotal(entity.getTotal())
                .build();
    }

    private DeliveryRoute toDeliveryRoute(DeliveryRouteEntity entity) {
        return DeliveryRoute.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .pickupAddress(entity.getPickupAddress())
                .deliveryAddress(entity.getDeliveryAddress())
                .distanceKm(entity.getDistanceKm())
                .estimatedMinutes(entity.getEstimatedMinutes())
                .status(entity.getStatus())
                .build();
    }
}
