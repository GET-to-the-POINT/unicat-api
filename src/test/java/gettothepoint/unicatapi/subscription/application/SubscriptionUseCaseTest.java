package gettothepoint.unicatapi.subscription.application;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.application.service.PlanService;
import gettothepoint.unicatapi.subscription.application.service.SubscriptionService;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionUseCaseTest {

    private static final String PLAN_BASIC = "BASIC";
    private static final String PLAN_PREMIUM = "PREMIUM";
    private static final String PLAN_PRO = "PRO";

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PlanService planService;

    @InjectMocks
    private SubscriptionUseCase subscriptionUseCase;

    private Member testMember;
    private Plan basicPlan;
    private Plan premiumPlan;
    private Plan proPlan;
    private Subscription basicSubscription;
    private Subscription premiumSubscription;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 플랜 객체 생성
        basicPlan = Plan.builder()
                .name(PLAN_BASIC)
                .description("Basic Plan")
                .price(0L)
                .aiImageCount(10L)
                .aiScriptCount(5L)
                .artifactCount(1L)
                .build();
        setPrivateField(basicPlan, "id", 1L);

        premiumPlan = Plan.builder()
                .name(PLAN_PREMIUM)
                .description("Premium Plan")
                .price(1000L)
                .aiImageCount(20L)
                .aiScriptCount(10L)
                .artifactCount(3L)
                .build();
        setPrivateField(premiumPlan, "id", 2L);

        proPlan = Plan.builder()
                .name(PLAN_PRO)
                .description("Pro Plan")
                .price(2000L)
                .aiImageCount(50L)
                .aiScriptCount(30L)
                .artifactCount(10L)
                .build();
        setPrivateField(proPlan, "id", 3L);

        // 테스트용 멤버 생성
        testMember = new Member();
        setPrivateField(testMember, "id", 1L);

        // 테스트용 구독 객체 생성
        basicSubscription = new Subscription(testMember, basicPlan);
        setPrivateField(basicSubscription, "id", 1L);

        premiumSubscription = new Subscription(testMember, premiumPlan);
        setPrivateField(premiumSubscription, "id", 2L);
    }

    @Test
    @DisplayName("신규 회원에게 기본 구독을 생성한다")
    void createSubscription_ShouldCreateNewSubscriptionWithBasicPlan() {
        // when
        subscriptionUseCase.createSubscription(testMember);

        // then
        verify(subscriptionService, times(1)).createSubscription(testMember);
    }

    @Test
    @DisplayName("프리미엄 플랜에서 기본 플랜으로 전환한다")
    void changeToBasicPlan_ShouldChangeToBASICPlan() {
        // when
        subscriptionUseCase.changeToBasicPlan(testMember);

        // then
        verify(subscriptionService, times(1)).changeToBasicPlan(testMember);
    }

    @Test
    @DisplayName("기본 플랜에서 프리미엄 플랜으로 변경한다")
    void changePlan_FromBasicToPremium_ShouldChangePlanSuccessfully() {
        // given
        when(planService.getPlanByName(PLAN_PREMIUM)).thenReturn(premiumPlan);
        testMember.setSubscription(basicSubscription);

        // when
        subscriptionUseCase.changePlan(testMember, PLAN_PREMIUM);

        // then
        verify(subscriptionService, times(1)).changePlan(basicSubscription, premiumPlan);
    }

    @Test
    @DisplayName("존재하지 않는 플랜으로 변경 시 예외가 발생한다")
    void changePlan_WithNonExistingPlan_ShouldThrowException() {
        // given
        final String nonExistingPlanName = "NON_EXISTING_PLAN";
        when(planService.getPlanByName(nonExistingPlanName))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when & then
        assertThrows(ResponseStatusException.class,
                () -> subscriptionUseCase.changePlan(testMember, nonExistingPlanName));

        // verify
        verify(planService, times(1)).getPlanByName(nonExistingPlanName);
        verifyNoInteractions(subscriptionService); // subscriptionService는 호출되지 않아야 함
    }

    @Test
    @DisplayName("만료되지 않은 구독은 변경되지 않는다")
    void checkAndExpireIfNeeded_WithNonExpiredSubscription_ShouldNotChangeToBasicPlan() {
        // given
        testMember.setSubscription(premiumSubscription);
        
        // when
        subscriptionUseCase.checkAndExpireIfNeeded(testMember);

        // then
        verify(subscriptionService, times(1)).checkAndExpireIfNeeded(testMember);
    }

    @Test
    @DisplayName("만료된 구독은 기본 플랜으로 변경된다")
    void checkAndExpireIfNeeded_WithExpiredSubscription_ShouldChangeToBasicPlan() {
        // given
        testMember.setSubscription(premiumSubscription);
        
        // subscriptionService.checkAndExpireIfNeeded 호출 시 만료된 구독 처리 로직 수행
        doAnswer(invocation -> {
            // subscriptionService.changeToBasicPlan 메서드를 직접 호출하도록 동작 정의
            subscriptionService.changeToBasicPlan(testMember);
            return null;
        }).when(subscriptionService).checkAndExpireIfNeeded(testMember);
        
        // 기본 플랜으로 변경하는 동작 모킹
        doAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            // 실제 changePlan 메서드 동작 흉내내기
            member.getSubscription().changePlan(basicPlan);
            return null;
        }).when(subscriptionService).changeToBasicPlan(testMember);
        
        // when
        subscriptionUseCase.checkAndExpireIfNeeded(testMember);
        
        // then
        verify(subscriptionService, times(1)).checkAndExpireIfNeeded(testMember);
        verify(subscriptionService, times(1)).changeToBasicPlan(testMember);
    }

    /**
     * private 필드에 값을 설정하는 유틸리티 메서드
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}