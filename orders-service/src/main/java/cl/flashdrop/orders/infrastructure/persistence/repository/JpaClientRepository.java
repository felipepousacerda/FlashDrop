package cl.flashdrop.orders.infrastructure.persistence.repository;

import cl.flashdrop.orders.infrastructure.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaClientRepository extends JpaRepository<ClientEntity, UUID> {

    @Query("SELECT c FROM ClientEntity c WHERE c.user.id = :userId")
    Optional<ClientEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT c FROM ClientEntity c ORDER BY c.id ASC LIMIT 1")
    Optional<ClientEntity> findFirst();
}
