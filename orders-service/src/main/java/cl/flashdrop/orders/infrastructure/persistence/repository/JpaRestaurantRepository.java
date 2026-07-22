package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, UUID> {

    @Query("SELECT r FROM RestaurantEntity r WHERE r.user.id = :userId")
    Optional<RestaurantEntity> findByUserId(@Param("userId") UUID userId);
}
