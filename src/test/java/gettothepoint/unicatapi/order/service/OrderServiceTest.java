package gettothepoint.unicatapi.order.service;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.payment.persistence.OrderRepository;
import gettothepoint.unicatapi.payment.application.OrderService;
import gettothepoint.unicatapi.subscription.domain.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Member member;
    private Plan plan;
    private Order order;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        plan = Plan.builder()
                .name("프리미엄 플랜")
                .price(10000L)
                .build();
        order = mock(Order.class);
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        try (MockedStatic<Order> mockedOrder = mockStatic(Order.class)) {
            // Arrange
            mockedOrder.when(() -> Order.createOrder(anyString(), anyLong(), any(Member.class), any(Plan.class)))
                       .thenReturn(order);
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.create(member, plan);

            // Assert
            assertNotNull(result);
            verify(orderRepository).save(any(Order.class));
            mockedOrder.verify(() -> Order.createOrder(anyString(), anyLong(), any(Member.class), any(Plan.class)));
        }
    }

    @Test
    void markAsDone_shouldMarkOrderAsDoneSuccessfully() {
        // given
        Order order = mock(Order.class);

        // when
        orderService.markAsDone(order);

        // then
        verify(order, times(1)).markDone();
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void getOrElseThrow_shouldReturnOrderWhenExists() {
        // given
        String orderId = "test-order-id";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when
        Order result = orderService.getOrElseThrow(orderId);

        // then
        assertNotNull(result);
        assertEquals(order, result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrElseThrow_shouldThrowExceptionWhenOrderNotExists() {
        // given
        String orderId = "non-existent-order-id";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.getOrElseThrow(orderId)
        );

        assertTrue(exception.getMessage().contains("주문이 존재하지 않습니다"));
        verify(orderRepository, times(1)).findById(orderId);
    }
}