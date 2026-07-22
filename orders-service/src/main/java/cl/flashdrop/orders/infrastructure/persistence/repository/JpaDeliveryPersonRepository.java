package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaDeliveryPersonRepository extends JpaRepository<DeliveryEntity, UUID> {

    @Query("SELECT d FROM DeliveryEntity d LEFT JOIN FETCH d.user u WHERE d.user.id = :userId")
    Optional<DeliveryEntity> findByUserIdWithUser(@Param("userId") UUID userId);
}
