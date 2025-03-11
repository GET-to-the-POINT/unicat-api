package gettothepoint.unicatapi.application.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.PaymentUtil;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentRequest;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.dto.payment.PaymentCancelServiceDto;
import gettothepoint.unicatapi.domain.entity.Order;
import gettothepoint.unicatapi.domain.entity.Payment;
import gettothepoint.unicatapi.domain.repository.OrderRepository;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentUtil paymentUtil;
    private final OrderService orderService;
    private final OrderRepository orderRepository;


    public CancelPaymentResponse cancelPayment(PaymentCancelServiceDto dto) {
        Payment originalPayment = paymentService.findById(dto.paymentId());
        String storedPaymentKey = originalPayment.getPaymentKey();

        CancelPaymentResponse cancelPaymentResponse = requestExternalCancel(storedPaymentKey, dto.cancelReason());
        originalPayment.setCancel(cancelPaymentResponse);

        Order order = originalPayment.getOrder();
        order.cancelOrder();

        paymentRepository.save(originalPayment);
        orderRepository.save(order);

        subscriptionService.cancelSubscriptionByPayment(originalPayment);
        return cancelPaymentResponse;
    }

    private CancelPaymentResponse requestExternalCancel(String paymentKey, String cancelReason) {
        String url = appProperties.toss().cancelUrl() + paymentKey + "/cancel";

        // 간단한 JSON 문자열 생성
        String requestBody = "{ \"cancelReason\": \"" + cancelReason + "\" }";

        String idempotencyKey = UUID.randomUUID().toString();
        String authorizationHeader = paymentUtil.createAuthorizationHeader();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authorizationHeader)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return sendCancelRequest(request);
    }

    private CancelPaymentResponse sendCancelRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), CancelPaymentResponse.class);
            } else {
                throw new RuntimeException("결제 취소 실패 - 상태 코드: " + response.statusCode() + ", 응답: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("결제 취소 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String convertToJson(CancelPaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류: " + e.getMessage(), e);
        }
    }
}
