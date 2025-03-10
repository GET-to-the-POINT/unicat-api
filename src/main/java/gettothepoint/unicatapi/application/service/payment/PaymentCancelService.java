package gettothepoint.unicatapi.application.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentRequest;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.entity.Payment;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
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


    public CancelPaymentResponse cancelPayment(Long paymentId, CancelPaymentRequest cancelRequest) {

        Payment originalPayment = paymentService.findById(paymentId); //payment id 조회
        String storedPaymentKey = originalPayment.getPaymentKey(); //payment 엔티티 안에 저장된 paymentKey 뽑아내기

        CancelPaymentResponse cancelPaymentResponse = requestExternalCancel(storedPaymentKey, cancelRequest); //외부 API 호출

        Payment canceledPayment = createCanceledPayment(originalPayment, cancelRequest, cancelPaymentResponse); //결제 취소 엔티티 저장
        paymentRepository.save(canceledPayment);

        subscriptionService.cancelSubscriptionByPayment(originalPayment);
        return cancelPaymentResponse;
    }

    private Payment createCanceledPayment (Payment original, CancelPaymentRequest cancelRequest, CancelPaymentResponse cancelPaymentResponse) {
        return Payment.builder()
                .paymentKey(original.getPaymentKey())           // 원래 결제와 동일한 결제 키 사용
                .productName(original.getProductName())
                .amount(original.getAmount())                     // 원래 결제 금액 (또는 취소 금액, 부분 취소 시 조정 가능)
                .payType(cancelPaymentResponse.getPayType())      // 취소 시 사용된 결제 수단
                .tossPaymentStatus(cancelPaymentResponse.getTossPaymentStatus()) // 취소 상태
                .order(original.getOrder())
                .member(original.getMember())
                .approvedAt(original.getApprovedAt())             // 원래 승인 일시 보존
                .canceledAt(LocalDateTime.now())                  // 취소 일시
                .cancelReason(cancelRequest.getCancelReason())    // 취소 사유
                .build();
    }

    private CancelPaymentResponse requestExternalCancel(String paymentKey, CancelPaymentRequest cancelRequest) {
        String url = appProperties.toss().cancelUrl() + paymentKey + "/cancel";
        String requestBody = convertToJson(cancelRequest);
        String idempotencyKey = UUID.randomUUID().toString();
        String authorizationHeader = createAuthorizationHeader();

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
            System.out.printf("Cancel API response status: %d, body: %s%n", response.statusCode(), response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), CancelPaymentResponse.class);
            } else {
                throw new RuntimeException("결제 취소 실패 - 상태 코드: " + response.statusCode() + ", 응답: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.printf("결제 취소 중 오류 발생: %s%n", e.getMessage());
            throw new RuntimeException("결제 취소 중 오류 발생: " + e.getMessage());
        }
    }

    private String convertToJson(CancelPaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류: " + e.getMessage(), e);
        }
    }

    private String createAuthorizationHeader() {
        String authString = appProperties.toss().secretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        return "Basic " + encodedAuth;
    }
}
