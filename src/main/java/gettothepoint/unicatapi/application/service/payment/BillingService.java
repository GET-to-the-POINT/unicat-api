package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.payment.BillingResponse;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final MemberRepository memberRepository;
    private final BillingRepository billingRepository;
    private final OrderService orderService;

    @Transactional
    public void saveBillingKey(String authKey, String email) {
        Member member = findMemberByEmail(email);
        billingRepository.findByMember(member)
                .ifPresentOrElse(
                        existing -> {},                         // 이미 있으면 아무 작업 없이 종료
                        () -> createAndSaveBilling(member, authKey) // 없으면 새로 생성 + 저장
                );
    }

    private void createAndSaveBilling(Member member, String authKey) {
        // TossPayments API 호출
        Map<String, Object> billingResponse = requestBillingKey(authKey, member.getEmail());

        String billingKey = (String) billingResponse.get("billingKey");
        String cardCompany = (String) billingResponse.get("cardCompany");
        String cardNumber = (String) billingResponse.get("cardNumber");
        String method = (String) billingResponse.get("method");

        // 회원의 최신 주문 정보 조회 (없으면 기본값 처리)
        orderService.findLatestOrderByMember(member);

        Billing billing = Billing.builder()
                .member(member)
                .billingKey(billingKey)
                .cardCompany(cardCompany)
                .cardNumber(cardNumber)
                .method(method)
                .lastPaymentDate(LocalDate.now())
                .build();

        billingRepository.save(billing);
    }

    private Map<String, Object> requestBillingKey(String authKey, String email) {
        HttpHeaders headers = createHeaders(encodeSecretKey());
        Map<String, String> requestBody = Map.of(
                "authKey", authKey,
                "customerKey", email
        );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.tosspayments.com/v1/billing/authorizations/issue",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "빌링키 발급 실패"));
    }

    private String encodeSecretKey() {
        String secretKeyWithColon = appProperties.toss().secretKey() + ":";
        return Base64.getEncoder().encodeToString(secretKeyWithColon.getBytes());
    }

    private HttpHeaders createHeaders(String encodedSecretKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."));
    }

    public void cancelSubscription(Long billingId) {
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing not found"));

        billing.cancelSubscription();
        billingRepository.save(billing);
    }

    @Transactional(readOnly = true)
    public List<BillingResponse> getAllBillings() {
        List<Billing> billings = billingRepository.findAll();
        return billings.stream()
                .map(billing -> BillingResponse.builder()
                        .lastPaymentDate(billing.getLastPaymentDate())
                        .id(billing.getId())
                        .billingKey(billing.getBillingKey())
                        .cardCompany(billing.getCardCompany())
                        .cardNumber(billing.getCardNumber())
                        .method(billing.getMethod())
                        .subscriptionStatus(billing.getSubscriptionStatus().toString())
                        .build())
                .toList();
    }
}
