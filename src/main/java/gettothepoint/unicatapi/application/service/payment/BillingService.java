package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
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

import java.time.LocalDate;
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
    private final OrderService orderService;

    /**
     * TossPayments API를 통해 빌링키를 발급받아 회원의 Billing 엔티티로 저장합니다.
     * 이미 빌링키가 존재하면 해당 키를 재사용합니다.
     */
    @Transactional
    public String saveBillingKey(String authKey, String customerKey, String email) {
        Member member = findMemberByEmail(email);
        return billingRepository.findByMember(member)
                .map(Billing::getBillingKey)
                .orElseGet(() -> createAndSaveBilling(member, authKey, customerKey).getBillingKey());
    }

    private Billing createAndSaveBilling(Member member, String authKey, String customerKey) {
        // TossPayments API 호출
        Map<String, Object> billingResponse = requestBillingKey(authKey, customerKey);

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
        return billing;
    }

    private Map<String, Object> requestBillingKey(String authKey, String customerKey) {
        HttpHeaders headers = createHeaders(encodeSecretKey());
        Map<String, String> requestBody = Map.of(
                "authKey", authKey,
                "customerKey", customerKey
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
}
