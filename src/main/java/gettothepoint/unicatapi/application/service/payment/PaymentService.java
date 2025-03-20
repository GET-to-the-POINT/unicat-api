package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentHistoryResponse;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Payment;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import gettothepoint.unicatapi.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final AppProperties appProperties;
    private final SubscriptionRepository subscriptionRepository;
    private final BillingRepository billingRepository;

    @Transactional
    public Map<String, Object> approveAutoPayment(String billingKey, PaymentApprovalRequest request) {
        Map<String, Object> responseBody = callTossBillingApi(billingKey, request);

        Order order = orderService.findById(request.getOrderId());
        order.markDone();
        paymentRepository.save(Payment.fromMap(responseBody, order));

        subscriptionRepository.save(
                        Subscription.builder()
                        .member(order.getMember())
                        .order(order)
                        .build()
        );

        Billing billing = billingRepository.findByBillingKey(billingKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing not found"));
        billing.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        billingRepository.save(billing);

        return responseBody;
    }

    private Map<String, Object> callTossBillingApi(String billingKey, PaymentApprovalRequest req) {
        String url = String.format("https://api.tosspayments.com/v1/billing/%s", billingKey);
        HttpEntity<PaymentApprovalRequest> entity = new HttpEntity<>(req, buildHeaders());

        return Optional.ofNullable(
                restTemplate.exchange(url, HttpMethod.POST, entity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}).getBody()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동 결제 승인 실패"));
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
