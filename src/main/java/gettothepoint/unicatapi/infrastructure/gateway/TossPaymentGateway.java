package gettothepoint.unicatapi.infrastructure.gateway;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway {

    private final AppProperties appProperties;

    public Map<String, Object> requestApproval(Order order, String billingKey, String customerKey) {
        String url = appProperties.toss().approveUrl() + "/" + billingKey;
        String secretKey = appProperties.toss().secretKey();
        String base64Secret = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                .amount(order.getAmount())
                .orderId(order.getId())
                .orderName(order.getOrderName())
                .customerKey(customerKey)
                .build();

        HttpResponse<Map> response = Unirest.post(url)
                .header("Authorization", "Basic " + base64Secret)
                .header("Content-Type", "application/json")
                .body(request)
                .asObject(Map.class);

        if (response.getStatus() != 200 || response.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동결제 승인 실패");
        }

        return response.getBody();
    }
}
