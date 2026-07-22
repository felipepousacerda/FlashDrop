package cl.flashdrop.orders.infrastructure.persistence.adapter;

import cl.flashdrop.orders.domain.model.ClientInfo;
import cl.flashdrop.orders.domain.model.DeliveryInfo;
import cl.flashdrop.orders.domain.port.DeliveryPort;
import cl.flashdrop.orders.infrastructure.persistence.entity.ClientEntity;
import cl.flashdrop.orders.infrastructure.persistence.entity.DeliveryEntity;
import cl.flashdrop.orders.infrastructure.persistence.repository.JpaClientRepository;
import cl.flashdrop.orders.infrastructure.persistence.repository.JpaDeliveryPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del puerto DeliveryPort usando JPA local.
 *
 * Gestiona la resolución de clientes y repartidores a partir de la
 * base de datos compartida de Supabase.
 *
 * DISEÑO PARA EVOLUCIÓN: En el futuro, puede dividirse en:
 * - {@code ClientHttpAdapter} → consulta a auth-service
 * - {@code DeliveryHttpAdapter} → consulta a delivery-service
 */
@Component
@RequiredArgsConstructor
public class DeliveryJpaAdapter implements DeliveryPort {

    private final JpaClientRepository clientRepo;
    private final JpaDeliveryPersonRepository deliveryPersonRepo;

    @Override
    public Optional<UUID> findClientIdByUserId(UUID userId) {
        if (userId != null) {
            return clientRepo.findByUserId(userId).map(ClientEntity::getId);
        }
        // Modo demo: si no hay userId, se usa el primer cliente disponible
        return clientRepo.findFirst().map(ClientEntity::getId);
    }

    @Override
    public Optional<ClientInfo> findClientById(UUID clientId) {
        return clientRepo.findById(clientId).map(c -> {
            var user = c.getUser();
            return ClientInfo.builder()
                    .clientId(c.getId())
                    .userId(user != null ? user.getId() : null)
                    .name(user != null ? user.getName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .email(user != null ? user.getEmail() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .build();
        });
    }

    @Override
    public Optional<UUID> findDeliveryIdByUserId(UUID userId) {
        return deliveryPersonRepo.findByUserIdWithUser(userId).map(DeliveryEntity::getId);
    }

    @Override
    public Optional<DeliveryInfo> findDeliveryById(UUID deliveryId) {
        return deliveryPersonRepo.findById(deliveryId).map(d -> {
            var user = d.getUser();
            return DeliveryInfo.builder()
                    .deliveryId(d.getId())
                    .userId(user != null ? user.getId() : null)
                    .name(user != null ? user.getName() : null)
                    .lastName(user != null ? user.getLastName() : null)
                    .phone(user != null ? user.getPhone() : null)
                    .vehicle(d.getVehicle())
                    .build();
        });
    }
}
