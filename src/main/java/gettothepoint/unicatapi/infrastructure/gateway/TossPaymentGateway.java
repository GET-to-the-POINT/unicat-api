package gettothepoint.unicatapi.infrastructure.gateway;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.ApiUtil;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final ApiUtil apiUtil;

    public Map<String, Object> requestApproval(Order order, String billingKey, String customerKey) {
        PaymentApprovalRequest request = PaymentApprovalRequest.builder()
                .amount(order.getAmount())
                .orderId(order.getId())
                .orderName(order.getOrderName())
                .customerKey(customerKey)
                .build();

        HttpEntity<PaymentApprovalRequest> entity = new HttpEntity<>(request, apiUtil.createHeaders(apiUtil.encodeSecretKey()));
        String url = appProperties.toss().approveUrl() + "/" + billingKey;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {}
        );

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동결제 승인 실패"));
    }
}

