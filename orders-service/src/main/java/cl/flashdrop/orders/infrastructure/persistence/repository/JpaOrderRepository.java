package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.OrderEntity;
import cl.flashdrop.orders.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.client c
        LEFT JOIN FETCH c.user cu
        LEFT JOIN FETCH o.restaurant r
        LEFT JOIN FETCH o.delivery d
        LEFT JOIN FETCH d.user du
        ORDER BY o.id DESC
        """)
    List<OrderEntity> findAllWithRelations();

    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.client c
        LEFT JOIN FETCH c.user cu
        LEFT JOIN FETCH o.restaurant r
        LEFT JOIN FETCH o.delivery d
        LEFT JOIN FETCH d.user du
        WHERE o.restaurant.id = :restaurantId
        ORDER BY o.id DESC
        """)
    List<OrderEntity> findAllByRestaurantIdWithRelations(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.client c
        LEFT JOIN FETCH c.user cu
        LEFT JOIN FETCH o.restaurant r
        LEFT JOIN FETCH o.delivery d
        LEFT JOIN FETCH d.user du
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product p
        LEFT JOIN FETCH o.route rt
        WHERE o.id = :orderId
        """)
    java.util.Optional<OrderEntity> findByIdWithAllRelations(@Param("orderId") UUID orderId);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.status = :status WHERE o.id = :orderId")
    void updateStatus(@Param("orderId") UUID orderId, @Param("status") String status);

    @Modifying
    @Query("""
        UPDATE OrderEntity o
        SET o.delivery.id = :deliveryId, o.status = :status
        WHERE o.id IN :orderIds
        AND o.status NOT IN :blockedStatuses
        """)
    int claimOrders(
            @Param("orderIds") List<UUID> orderIds,
            @Param("deliveryId") UUID deliveryId,
            @Param("status") String status,
            @Param("blockedStatuses") List<String> blockedStatuses);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.delivery.id = :deliveryId AND o.status IN :activeStatuses")
    int countActiveByDelivery(
            @Param("deliveryId") UUID deliveryId,
            @Param("activeStatuses") List<String> activeStatuses);

    @Query("SELECT o FROM OrderEntity o WHERE o.id IN :orderIds")
    List<OrderEntity> findByIds(@Param("orderIds") List<UUID> orderIds);
}
