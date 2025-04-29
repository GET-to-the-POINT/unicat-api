package gettothepoint.unicatapi.member.domain.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.subscription.domain.Subscription;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record MemberResponse(
        @Schema(example = "1")
        UUID id,
        @Schema(example = "김유니")
        String name,
        @Schema(example = "01012345678")
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

record SubscriptionResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "BASIC")
        String plan,
        @Schema(example = "2025-03-28T18:12:01.574263")
        LocalDateTime startDate,
        @Schema(example =  "9999-12-31T23:59:59")
        LocalDateTime endDate) {
public static SubscriptionResponse fromEntity(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getPlan().getName(), subscription.getStartDate(), subscription.getEndDate());
    }
}

record BillingResponse(
        @Schema(example = "1")
        Long id,
        @Schema(example = "false")
        Boolean recurring ,
        @Schema(example = "2025-04-28")
        LocalDate LastPaymentDate,
        @Schema(example = "현대")
        String cardCompany) {
    public static BillingResponse fromEntity(Billing billing) {
        return new BillingResponse(billing.getId(), billing.getRecurring(), billing.getLastPaymentDate(), billing.getCardCompany());
    }
}