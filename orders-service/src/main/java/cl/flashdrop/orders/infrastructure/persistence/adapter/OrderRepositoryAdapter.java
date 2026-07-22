package cl.flashdrop.orders.infrastructure.persistence.adapter;

import cl.flashdrop.orders.domain.model.*;
import cl.flashdrop.orders.domain.port.OrderRepositoryPort;
import cl.flashdrop.orders.infrastructure.persistence.entity.*;
import cl.flashdrop.orders.infrastructure.persistence.mapper.OrderMapper;
import cl.flashdrop.orders.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del puerto de persistencia de pedidos usando Spring Data JPA.
 *
 * Traduce entre modelos de dominio y entidades JPA.
 * Puede ser reemplazado por un repositorio diferente sin tocar el dominio.
 */
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final JpaOrderRepository orderRepo;
    private final JpaDeliveryRouteRepository routeRepo;
    private final JpaClientRepository clientRepo;
    private final JpaRestaurantRepository restaurantRepo;
    private final JpaDeliveryPersonRepository deliveryPersonRepo;
    private final JpaProductRepository productRepo;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        ClientEntity client = clientRepo.findById(order.getClientId())
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado: " + order.getClientId()));
        RestaurantEntity restaurant = restaurantRepo.findById(order.getRestaurantId())
                .orElseThrow(() -> new IllegalStateException("Restaurante no encontrado: " + order.getRestaurantId()));

        OrderEntity entity = OrderEntity.builder()
                .client(client)
                .restaurant(restaurant)
                .delivery(null)
                .status(order.getStatus().getValue())
                .address(order.getAddress())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod().getValue())
                .build();

        // Mapear ítems
        List<OrderItemEntity> itemEntities = order.getItems().stream().map(item -> {
            ProductEntity product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado: " + item.getProductId()));
            return OrderItemEntity.builder()
                    .order(entity)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .total(item.getLineTotal())
                    .build();
        }).collect(Collectors.toList());

        entity.setItems(itemEntities);
        OrderEntity saved = orderRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepo.findByIdWithAllRelations(id).map(mapper::toDomainWithDetail);
    }

    @Override
    public List<Order> findAll(UUID restaurantId) {
        List<OrderEntity> entities = restaurantId != null
                ? orderRepo.findAllByRestaurantIdWithRelations(restaurantId)
                : orderRepo.findAllWithRelations();
        return entities.stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(UUID orderId, OrderStatus status) {
        orderRepo.updateStatus(orderId, status.getValue());
    }

    @Override
    public int claimOrders(List<UUID> orderIds, UUID deliveryId, OrderStatus status) {
        List<String> blockedStatuses = List.of(
                OrderStatus.EN_CAMINO.getValue(),
                OrderStatus.RETIRADO.getValue(),
                OrderStatus.ENTREGADO.getValue()
        );
        return orderRepo.claimOrders(orderIds, deliveryId, status.getValue(), blockedStatuses);
    }

    @Override
    public int countActiveOrdersByDelivery(UUID deliveryId) {
        return orderRepo.countActiveByDelivery(deliveryId, List.of(
                OrderStatus.EN_CAMINO.getValue(),
                OrderStatus.RETIRADO.getValue()
        ));
    }

    @Override
    public List<Order> findByIdsForClaim(List<UUID> orderIds) {
        return orderRepo.findByIds(orderIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void saveRoute(DeliveryRoute route) {
        OrderEntity orderRef = orderRepo.getReferenceById(route.getOrderId());
        DeliveryRouteEntity routeEntity = DeliveryRouteEntity.builder()
                .order(orderRef)
                .pickupAddress(route.getPickupAddress())
                .deliveryAddress(route.getDeliveryAddress())
                .distanceKm(route.getDistanceKm())
                .estimatedMinutes(route.getEstimatedMinutes())
                .status(route.getStatus())
                .build();
        routeRepo.save(routeEntity);
    }

    @Override
    public void updateRouteStatus(List<UUID> orderIds, String status) {
        routeRepo.updateStatusByOrderIds(orderIds, status);
    }

    @Override
    public void updateRouteStatusByOrder(UUID orderId, String status) {
        routeRepo.updateStatusByOrderId(orderId, status);
    }

    @Override
    public List<Order> findAllRoutesWithOrders() {
        return routeRepo.findAllWithOrders().stream()
                .map(r -> mapper.toDomainWithRoute(r.getOrder(), r))
                .collect(Collectors.toList());
    }
}
