package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.DeliveryRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaDeliveryRouteRepository extends JpaRepository<DeliveryRouteEntity, UUID> {

    @Modifying
    @Query("UPDATE DeliveryRouteEntity r SET r.status = :status WHERE r.order.id IN :orderIds")
    void updateStatusByOrderIds(@Param("orderIds") List<UUID> orderIds, @Param("status") String status);

    @Modifying
    @Query("UPDATE DeliveryRouteEntity r SET r.status = :status WHERE r.order.id = :orderId")
    void updateStatusByOrderId(@Param("orderId") UUID orderId, @Param("status") String status);

    @Query("""
        SELECT DISTINCT r FROM DeliveryRouteEntity r
        LEFT JOIN FETCH r.order o
        LEFT JOIN FETCH o.client c
        LEFT JOIN FETCH c.user cu
        LEFT JOIN FETCH o.restaurant rs
        LEFT JOIN FETCH o.delivery d
        LEFT JOIN FETCH d.user du
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product p
        ORDER BY r.distanceKm ASC, r.id DESC
        """)
    List<DeliveryRouteEntity> findAllWithOrders();
}
