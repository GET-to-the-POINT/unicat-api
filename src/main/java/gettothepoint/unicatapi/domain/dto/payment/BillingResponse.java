package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BillingResponse {
    private LocalDate lastPaymentDate;
    private Long id;
    private String billingKey;
    private String cardCompany;
    private String cardNumber;
    private String method;
    private String subscriptionStatus;
}