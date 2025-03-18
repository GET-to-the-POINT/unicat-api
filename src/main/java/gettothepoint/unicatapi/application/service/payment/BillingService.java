package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final MemberRepository memberRepository;
    private final BillingRepository billingRepository;
    private final PaymentService paymentService;
    private final OrderService orderService;

    private Map<String, Object> requestBillingKey(String authKey, String customerKey) {
        String encodedSecretKey = encodeSecretKey();

        HttpHeaders headers = createHeaders(encodedSecretKey);
        HttpEntity<Map<String, String>> requestEntity = createRequestEntity(authKey, customerKey, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.tosspayments.com/v1/billing/authorizations/issue",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "❌ 빌링키 발급 실패"));
    }

    /**
     * 빌링키를 발급받고, 회원의 Billing 엔티티로 저장합니다.
     * OrderRequest 등 추가 정보 없이 authKey, customerKey만 사용합니다.
     */
    @Transactional
    public String SaveBillingKey(String authKey, String customerKey, String email) {
        Member member = findMemberByEmail(email);
        Optional<Billing> existingBilling = billingRepository.findByMember(member);
        if (existingBilling.isPresent()) {
            return existingBilling.get().getBillingKey();
        }

        // TossPayments API 호출 (authKey, customerKey로만 요청)
        Map<String, Object> billingResponse = requestBillingKey(authKey, customerKey);

        // 응답에서 필요한 값 추출
        String billingKey = (String) billingResponse.get("billingKey");
        String cardCompany = (String) billingResponse.get("cardCompany");
        String cardNumber = (String) billingResponse.get("cardNumber");
        String method = (String) billingResponse.get("method");

        Billing billing = Billing.builder()
                .member(member)
                .billingKey(billingKey)
                .cardCompany(cardCompany)
                .cardNumber(cardNumber)
                .method(method)
                // 주문 관련 정보가 없으므로 기본값 처리
                .amount(0L)
                .lastPaymentDate(LocalDateTime.now())
                .build();

        billingRepository.save(billing);
        return billingKey;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));
    }

    private String encodeSecretKey() {
        return Base64.getEncoder()
                .encodeToString((appProperties.toss().secretKey() + ":").getBytes());
    }

    private HttpHeaders createHeaders(String encodedSecretKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpEntity<Map<String, String>> createRequestEntity(String authKey, String customerKey, HttpHeaders headers) {
        Map<String, String> requestBody = Map.of(
                "authKey", authKey,
                "customerKey", customerKey
        );
        return new HttpEntity<>(requestBody, headers);
    }
    @Transactional
    public void autoPayment(String email, String billingKey, OrderRequest orderRequest) {
        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));

        // 2. 가주문 생성
        Order order = orderService.createOrder(orderRequest, member.getId());

        // 3. PaymentApprovalRequest 생성 (주문 정보와 회원 정보 기반)
        PaymentApprovalRequest approvalRequest = new PaymentApprovalRequest();
        approvalRequest.setAmount(orderRequest.getAmount());
        approvalRequest.setCustomerKey(member.getCustomerKey());
        approvalRequest.setOrderId(order.getId());
        approvalRequest.setOrderName(orderRequest.getOrderName());

        // 4. 자동 결제 승인 요청
        paymentService.approveAutoPayment(billingKey, approvalRequest);

    }
}