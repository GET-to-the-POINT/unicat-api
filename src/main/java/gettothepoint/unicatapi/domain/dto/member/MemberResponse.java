package gettothepoint.unicatapi.domain.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;

import java.time.LocalDate;
import java.util.Optional;

public record MemberResponse(Long id,
                             String name,
                             String phoneNumber,
                             SubscriptionResponse subscription,
                             @JsonInclude(JsonInclude.Include.NON_NULL)
                             BillingResponse billing
                             ) {
    public static MemberResponse fromEntity(Member member) {

        SubscriptionResponse subscriptionResponse = SubscriptionResponse.fromEntity(member.getSubscription());

        BillingResponse billingResponse = Optional.ofNullable(member.getBilling())
                .map(BillingResponse::fromEntity)
                .orElse(null);

        return new MemberResponse(member.getId(), member.getName(), member.getPhoneNumber(), subscriptionResponse, billingResponse);
    }
}

record SubscriptionResponse(Long id, String plan, String startDate, String endDate) {
public static SubscriptionResponse fromEntity(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getPlan().getName(), subscription.getStartDate().toString(), subscription.getEndDate().toString());
    }
}

record BillingResponse(Long id, Boolean recurring , LocalDate LastPaymentDate, String cardCompany) {
    public static BillingResponse fromEntity(Billing billing) {
        return new BillingResponse(billing.getId(), billing.getRecurring(), billing.getLastPaymentDate(), billing.getCardCompany());
    }
}