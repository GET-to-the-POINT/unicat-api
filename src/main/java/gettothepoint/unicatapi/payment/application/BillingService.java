package gettothepoint.unicatapi.payment.application;

import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.common.properties.TossProperties;
import gettothepoint.unicatapi.common.util.ApiUtil;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.payment.persistence.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final RestTemplate restTemplate;
    private final TossProperties tossProperties;
    private final BillingRepository billingRepository;
    private final MemberService memberService;
    private final ApiUtil apiUtil;

    public void saveBillingKey(String authKey, UUID memberId) {
        Member member = memberService.getOrElseThrow(memberId);
        issueSuccessAndCreate(member, authKey);
    }

    private void issueSuccessAndCreate(Member member, String authKey) {
        Map<String, Object> billingResponse = requestBillingKey(authKey, member.getId().toString());

        String billingKey = (String) billingResponse.get("billingKey");
        String cardCompany = (String) billingResponse.get("cardCompany");
//        String cardNumber = (String) billingResponse.get("cardNumber");
//        String method = (String) billingResponse.get("method");

        Billing membersBilling =billingRepository.findByMember(member).orElse(null);
        if (membersBilling != null) {
            membersBilling.setBillingKey(billingKey);
            membersBilling.setCardCompany(cardCompany);
        } else {
            membersBilling = Billing.builder()
                    .member(member)
                    .billingKey(billingKey)
                    .cardCompany(cardCompany)
                    .build();
        }

        member.setBilling(membersBilling);

        billingRepository.save(membersBilling);
        memberService.update(member);
    }

    private Map<String, Object> requestBillingKey(String authKey, String customerKey) {
        HttpHeaders headers = apiUtil.createHeaders(apiUtil.encodeSecretKey());
        Map<String, String> requestBody = Map.of(
                "authKey", authKey,
                "customerKey", customerKey
        );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tossProperties.billingUrl(),
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "빌링키 발급 실패"));
    }

    public void applyRecurring(Billing billing) {
        billing.recurring();
        billingRepository.save(billing);
    }


    public void cancelRecurringByMember(UUID memberId) {
        Member member = memberService.getOrElseThrow(memberId);
        Billing billing = billingRepository.findByMember(member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing not found"));

        billing.cancelRecurring();
        billingRepository.save(billing);
    }
}
