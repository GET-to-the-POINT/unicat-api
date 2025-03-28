package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final AppProperties appProperties;
    private final BillingRepository billingRepository;
    private final MemberService memberService;

    public void saveBillingKey(String authKey, Long memberId) {
        Member member = memberService.getOrElseThrow(memberId);
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
        String secretKey = appProperties.toss().secretKey();
        String base64Secret = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        HttpResponse<Map<String, Object>> response = Unirest.post(appProperties.toss().billingUrl())
                .header("Authorization", "Basic " + base64Secret)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "authKey", authKey,
                        "customerKey", email
                ))
                .asObject(Map.class);

        if (response.getStatus() != 200 || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "빌링키 발급 실패");
        }

        return response.getBody();
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
