package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.ApiUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final BillingRepository billingRepository;
    private final MemberService memberService;
    private final ApiUtil apiUtil;

    public void saveBillingKey(String authKey, String email) {
        Member member = memberService.getOrElseThrow(email);
        issueSuccessAndCreate(member, authKey);
    }

    private void issueSuccessAndCreate(Member member, String authKey) {
        Map<String, Object> billingResponse = requestBillingKey(authKey, member.getEmail());

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

    private Map<String, Object> requestBillingKey(String authKey, String email) {
        HttpHeaders headers = apiUtil.createHeaders(apiUtil.encodeSecretKey());
        Map<String, String> requestBody = Map.of(
                "authKey", authKey,
                "customerKey", email
        );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                appProperties.toss().billingUrl(),
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


    public void cancelRecurringByMember(Long memberId) {
        Member member = memberService.getOrElseThrow(memberId);
        Billing billing = billingRepository.findByMember(member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing not found"));

        billing.cancelRecurring();
        billingRepository.save(billing);
    }
}
