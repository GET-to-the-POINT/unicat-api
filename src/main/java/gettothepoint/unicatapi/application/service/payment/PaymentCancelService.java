package gettothepoint.unicatapi.application.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentRequest;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.entity.CancelPayment;
import gettothepoint.unicatapi.domain.entity.Payment;
import gettothepoint.unicatapi.domain.repository.CancelPaymentRepository;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
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
    private final CancelPaymentRepository cancelPaymentRepository;
    private final SubscriptionService subscriptionService;


    public CancelPaymentResponse cancelPayment(Long paymentId, CancelPaymentRequest cancelRequest) {

        Payment payment = paymentService.findById(paymentId); //payment id 조회
        String storedPaymentKey = payment.getPaymentKey(); //payment 엔티티 안에 저장된 paymentKey 뽑아내기

        CancelPaymentResponse cancelPaymentResponse = requestExternalCancel(storedPaymentKey, cancelRequest); //외부 API 호출
        updatePaymentStatus(payment, cancelPaymentResponse); //결제 상태 업데이트
        saveCancelPayment(payment, cancelRequest, cancelPaymentResponse); //결제 취소 엔티티 저장
        subscriptionService.cancelSubscriptionByPayment(payment);
        return cancelPaymentResponse;
    }

    private void updatePaymentStatus(Payment payment, CancelPaymentResponse cancelPaymentResponse) {
        TossPaymentStatus status = cancelPaymentResponse.getTossPaymentStatus();
        PayType payType = cancelPaymentResponse.getPayType();
        payment.setTossPaymentStatus(status);
        payment.setPayType(payType);
        paymentRepository.save(payment);
    }

    private void saveCancelPayment(Payment payment, CancelPaymentRequest cancelRequest, CancelPaymentResponse cancelPaymentResponse) {
        CancelPayment cancelPaymentEntity = CancelPayment.builder()
                .orderId(payment.getOrder().getId())
                .orderName(payment.getOrder().getOrderName())
                .paymentKey(payment.getPaymentKey())
                .cancelReason(cancelRequest.getCancelReason())
                .cancelAmount(cancelRequest.getCancelAmount())
                .canceledAt(LocalDateTime.now())
                .status(cancelPaymentResponse.getTossPaymentStatus())
                .method(cancelPaymentResponse.getPayType())
                .payment(payment)
                .build();
        cancelPaymentRepository.save(cancelPaymentEntity);
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
