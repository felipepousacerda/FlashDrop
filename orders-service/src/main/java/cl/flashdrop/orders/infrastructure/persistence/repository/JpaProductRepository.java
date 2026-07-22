package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.restaurant r WHERE p.id IN :ids")
    List<ProductEntity> findByIdsWithRestaurant(@Param("ids") List<UUID> ids);
}
