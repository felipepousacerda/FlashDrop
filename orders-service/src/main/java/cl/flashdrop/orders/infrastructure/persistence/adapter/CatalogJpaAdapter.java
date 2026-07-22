package cl.flashdrop.orders.infrastructure.persistence.adapter;

import cl.flashdrop.orders.domain.model.ProductInfo;
import cl.flashdrop.orders.domain.model.RestaurantInfo;
import cl.flashdrop.orders.domain.port.CatalogPort;
import cl.flashdrop.orders.infrastructure.persistence.entity.ProductEntity;
import cl.flashdrop.orders.infrastructure.persistence.repository.JpaProductRepository;
import cl.flashdrop.orders.infrastructure.persistence.repository.JpaRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del puerto CatalogPort usando JPA local.
 *
 * Consulta la base de datos compartida de Supabase para obtener
 * los precios actualizados de los productos.
 *
 * DISEÑO PARA EVOLUCIÓN: En el futuro, esta clase puede ser reemplazada
 * por {@code CatalogHttpAdapter} que consulte a catalog-service via HTTP
 * sin modificar ningún caso de uso ni regla de negocio.
 */
@Component
@RequiredArgsConstructor
public class CatalogJpaAdapter implements CatalogPort {

    private final JpaProductRepository productRepo;
    private final JpaRestaurantRepository restaurantRepo;

    @Override
    public List<ProductInfo> findProductsByIds(List<UUID> productIds) {
        return productRepo.findByIdsWithRestaurant(productIds).stream()
                .map(this::toProductInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RestaurantInfo> findRestaurantById(UUID restaurantId) {
        return restaurantRepo.findById(restaurantId).map(r -> RestaurantInfo.builder()
                .restaurantId(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .build());
    }

    @Override
    public Optional<UUID> findRestaurantIdByUserId(UUID userId) {
        return restaurantRepo.findByUserId(userId).map(r -> r.getId());
    }

    private ProductInfo toProductInfo(ProductEntity entity) {
        return ProductInfo.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant().getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .image(entity.getImage())
                .price(entity.getPrice())
                .available(Boolean.TRUE.equals(entity.getIsAvailable()))
                .build();
    }
}
