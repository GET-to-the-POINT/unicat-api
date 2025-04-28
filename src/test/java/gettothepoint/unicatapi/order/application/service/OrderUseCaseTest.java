package gettothepoint.unicatapi.order.application.service;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.order.domain.entity.Order;
import gettothepoint.unicatapi.subscription.application.service.PlanService;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    @Mock
    private MemberService memberService;

    @Mock
    private OrderService orderService;

    @Mock
    private PlanService planService;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private Member testMember;
    private Plan testPlan;
    private Order testOrder;
    private final Long MEMBER_ID = 1L;
    private final Long PLAN_ID = 1L;
    private final String ORDER_ID = "order-uuid-123";

    @BeforeEach
    void setUp() {
        // 테스트용 객체 생성
        testMember = new Member();
        testPlan = Plan.builder()
                .name("PREMIUM")
                .price(10000L)
                .build();
        testOrder = Order.createOrder(testPlan.getName(), testPlan.getPrice(), testMember, testPlan);
    }

    @Test
    @DisplayName("주문 생성 성공 테스트")
    void create_ShouldCreateNewOrderSuccessfully() {
        // given
        when(memberService.getOrElseThrow(MEMBER_ID)).thenReturn(testMember);
        when(planService.getOrElseThrow(PLAN_ID)).thenReturn(testPlan);
        when(orderService.create(testMember, testPlan)).thenReturn(testOrder);

        // when
        Order createdOrder = orderUseCase.create(MEMBER_ID, PLAN_ID);

        // then
        assertNotNull(createdOrder);
        verify(memberService, times(1)).getOrElseThrow(MEMBER_ID);
        verify(planService, times(1)).getOrElseThrow(PLAN_ID);
        verify(orderService, times(1)).create(testMember, testPlan);
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 주문 생성 시 예외 발생")
    void create_WithNonExistingMember_ShouldThrowException() {
        // given
        when(memberService.getOrElseThrow(MEMBER_ID)).thenThrow(new IllegalStateException("회원이 존재하지 않습니다."));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderUseCase.create(MEMBER_ID, PLAN_ID));
        
        assertEquals("회원이 존재하지 않습니다.", exception.getMessage());
        verify(memberService, times(1)).getOrElseThrow(MEMBER_ID);
        verifyNoInteractions(planService);
        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("존재하지 않는.플랜으로 주문 생성 시 예외 발생")
    void create_WithNonExistingPlan_ShouldThrowException() {
        // given
        when(memberService.getOrElseThrow(MEMBER_ID)).thenReturn(testMember);
        when(planService.getOrElseThrow(PLAN_ID)).thenThrow(new IllegalStateException("플랜이 존재하지 않습니다."));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderUseCase.create(MEMBER_ID, PLAN_ID));
        
        assertEquals("플랜이 존재하지 않습니다.", exception.getMessage());
        verify(memberService, times(1)).getOrElseThrow(MEMBER_ID);
        verify(planService, times(1)).getOrElseThrow(PLAN_ID);
        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("주문 완료 처리 성공 테스트")
    void markAsDone_ShouldMarkOrderAsDoneSuccessfully() {
        // given
        when(orderService.getOrElseThrow(ORDER_ID)).thenReturn(testOrder);
        doNothing().when(orderService).markAsDone(testOrder);

        // when
        orderUseCase.markAsDone(ORDER_ID);

        // then
        verify(orderService, times(1)).getOrElseThrow(ORDER_ID);
        verify(orderService, times(1)).markAsDone(testOrder);
    }

    @Test
    @DisplayName("존재하지 않는 주문 완료 처리 시 예외 발생")
    void markAsDone_WithNonExistingOrder_ShouldThrowException() {
        // given
        when(orderService.getOrElseThrow(ORDER_ID))
                .thenThrow(new IllegalStateException("주문이 존재하지 않습니다: " + ORDER_ID));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> orderUseCase.markAsDone(ORDER_ID));
        
        assertEquals("주문이 존재하지 않습니다: " + ORDER_ID, exception.getMessage());
        verify(orderService, times(1)).getOrElseThrow(ORDER_ID);
        verify(orderService, never()).markAsDone(any(Order.class));
    }
}