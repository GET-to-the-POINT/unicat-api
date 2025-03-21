package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentHistoryResponse;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Payment;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final AppProperties appProperties;
    private final BillingRepository billingRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public Map<String, Object> approveAutoPayment(String memberEmail) {
        Order order = orderRepository.findFirstByMember_EmailAndStatusOrderByCreatedAtDesc(memberEmail, TossPaymentStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "진행 중인 주문이 없습니다."));

        Billing billing = billingRepository.findByMember(order.getMember())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing 정보가 없습니다."));
        Member member = order.getMember();

        PaymentApprovalRequest req = PaymentApprovalRequest.builder()
                .amount(order.getAmount())
                .customerKey(memberEmail)
                .orderId(order.getId())
                .orderName(order.getOrderName())
                .build();

        String url = String.format("%s/%s", appProperties.toss().approveUrl(), billing.getBillingKey());
        HttpEntity<PaymentApprovalRequest> entity = new HttpEntity<>(req, buildHeaders());

        Map<String, Object> response = Optional.ofNullable(
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                ).getBody()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동 결제 승인 실패"));

        order.markDone();
        paymentRepository.save(Payment.fromMap(response, order));

        billing.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        billing.updateLastPaymentDate(LocalDate.now());
        billingRepository.save(billing);

        subscriptionRepository.save(
                Subscription.builder()
                        .member(member)
                        .order(order)
                        .membershipTier(order.getMembershipTier())
                        .startDate(LocalDateTime.now())
                        .build()
        );

        return response;
    }
    private HttpHeaders buildHeaders() {
        String encoded = Base64.getEncoder()
                .encodeToString((appProperties.toss().secretKey() + ":").getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public List<PaymentHistoryResponse> findPaymentHistoryByMember(String email) {
        List<Payment> payments = paymentRepository.findByOrder_Member_Email(email);
        return payments.stream()
                .map(PaymentHistoryResponse::fromEntity)
                .toList();
    }
}