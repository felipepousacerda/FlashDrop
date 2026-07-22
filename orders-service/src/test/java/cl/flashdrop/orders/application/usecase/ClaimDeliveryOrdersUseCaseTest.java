package cl.flashdrop.orders.application.usecase;

import cl.flashdrop.orders.domain.exception.OrderDomainException;
import cl.flashdrop.orders.domain.model.Order;
import cl.flashdrop.orders.domain.model.OrderStatus;
import cl.flashdrop.orders.domain.port.DeliveryPort;
import cl.flashdrop.orders.domain.port.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimDeliveryOrdersUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private DeliveryPort deliveryPort;

    @InjectMocks
    private ClaimDeliveryOrdersUseCase claimDeliveryOrdersUseCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(claimDeliveryOrdersUseCase, "maxClaimPerRoute", 3);
    }

    @Test
    void shouldClaimOrdersSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        List<UUID> orderIds = List.of(orderId1, orderId2);
        UUID deliveryId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        when(deliveryPort.findDeliveryIdByUserId(userId)).thenReturn(Optional.of(deliveryId));
        when(orderRepository.countActiveOrdersByDelivery(deliveryId)).thenReturn(0);

        Order order1 = Order.builder().id(orderId1).restaurantId(restaurantId).status(OrderStatus.NUEVO_PEDIDO).build();
        Order order2 = Order.builder().id(orderId2).restaurantId(restaurantId).status(OrderStatus.NUEVO_PEDIDO).build();
        when(orderRepository.findByIdsForClaim(any())).thenReturn(List.of(order1, order2));

        when(orderRepository.claimOrders(any(), eq(deliveryId), eq(OrderStatus.EN_CAMINO))).thenReturn(2);

        assertDoesNotThrow(() -> claimDeliveryOrdersUseCase.execute(userId, orderIds));

        verify(orderRepository).claimOrders(any(), eq(deliveryId), eq(OrderStatus.EN_CAMINO));
        verify(orderRepository).updateRouteStatus(any(), eq("En camino"));
    }

    @Test
    void shouldThrowExceptionWhenClaimingTooManyOrders() {
        UUID userId = UUID.randomUUID();
        List<UUID> orderIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()); // 4 orders

        OrderDomainException exception = assertThrows(OrderDomainException.class,
                () -> claimDeliveryOrdersUseCase.execute(userId, orderIds));

        assertTrue(exception.getMessage().contains("Debes seleccionar entre 1 y 3 pedidos"));
        verifyNoInteractions(deliveryPort, orderRepository);
    }

    @Test
    void shouldThrowExceptionWhenDriverHasActiveOrders() {
        UUID userId = UUID.randomUUID();
        List<UUID> orderIds = List.of(UUID.randomUUID());
        UUID deliveryId = UUID.randomUUID();

        when(deliveryPort.findDeliveryIdByUserId(userId)).thenReturn(Optional.of(deliveryId));
        when(orderRepository.countActiveOrdersByDelivery(deliveryId)).thenReturn(1); // has active order

        OrderDomainException exception = assertThrows(OrderDomainException.class,
                () -> claimDeliveryOrdersUseCase.execute(userId, orderIds));

        assertTrue(exception.getMessage().contains("Ya tienes pedidos en ruta"));
        verify(orderRepository, never()).findByIdsForClaim(any());
    }

    @Test
    void shouldThrowExceptionWhenOrdersAreFromDifferentRestaurants() {
        UUID userId = UUID.randomUUID();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        List<UUID> orderIds = List.of(orderId1, orderId2);
        UUID deliveryId = UUID.randomUUID();

        when(deliveryPort.findDeliveryIdByUserId(userId)).thenReturn(Optional.of(deliveryId));
        when(orderRepository.countActiveOrdersByDelivery(deliveryId)).thenReturn(0);

        Order order1 = Order.builder().id(orderId1).restaurantId(UUID.randomUUID()).status(OrderStatus.NUEVO_PEDIDO).build();
        Order order2 = Order.builder().id(orderId2).restaurantId(UUID.randomUUID()).status(OrderStatus.NUEVO_PEDIDO).build(); // different restaurant
        when(orderRepository.findByIdsForClaim(any())).thenReturn(List.of(order1, order2));

        OrderDomainException exception = assertThrows(OrderDomainException.class,
                () -> claimDeliveryOrdersUseCase.execute(userId, orderIds));

        assertTrue(exception.getMessage().contains("Solo puedes agrupar pedidos del mismo restaurante"));
        verify(orderRepository, never()).claimOrders(any(), any(), any());
    }
}
