package taeniverse.unicatApi.mvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentRequest;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentResponse;
import taeniverse.unicatApi.mvc.model.entity.CancelPayment;
import taeniverse.unicatApi.mvc.model.entity.Payment;
import taeniverse.unicatApi.mvc.repository.CancelPaymentRepository;
import taeniverse.unicatApi.mvc.repository.PaymentRepository;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class PaymentCancelService {

    // 외부 결제 API URL
    private static final String API_URL = "https://api.tosspayments.com/v1/payments/";

    @Value("${toss.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final CancelPaymentRepository cancelPaymentRepository;
    private final PaymentRepository paymentRepository;

    public PaymentCancelService(ObjectMapper objectMapper,
                                CancelPaymentRepository cancelPaymentRepository,
                                PaymentRepository paymentRepository) {
        this.objectMapper = objectMapper;
        this.cancelPaymentRepository = cancelPaymentRepository;
        this.paymentRepository = paymentRepository;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * 결제 취소 프로세스
     * 1. Payment 엔티티 조회 및 입력값 검증
     * 2. 외부 결제 API 호출
     * 3. Payment 상태를 CANCEL로 업데이트
     * 4. CancelPayment 엔티티 생성 및 저장
     *
     * @param paymentKey 결제 고유 키 (외부 API 및 CancelPayment 조회용)
     * @param cancelRequest 취소 요청 데이터 (취소 사유, 고객 정보 등)
     * @return 저장된 CancelPayment 엔티티
     */
    public CancelPayment cancelPayment(String paymentKey, CancelPaymentRequest cancelRequest) {
        // 1. Payment 엔티티 조회 (예시: orderId로 조회한다고 가정)
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new RuntimeException("Payment not found with key: " + paymentKey));

        // 2. 입력값 검증 (취소 사유 필수)
        if (cancelRequest.getCancelReason() == null || cancelRequest.getCancelReason().trim().isEmpty()) {
            throw new RuntimeException("취소 사유는 필수입니다.");
        }

        // 3. 외부 결제 취소 API 호출
        CancelPayment cancelPaymentFromApi = requestExternalCancel(paymentKey, cancelRequest);

        // 4. Payment 엔티티 상태 업데이트 및 저장
        payment = Payment.builder()
                .id(payment.getId())
                .order(payment.getOrder())
                .tossPaymentStatus(TossPaymentStatus.CANCEL)  // 상태 변경: CANCEL
                .build();
        paymentRepository.save(payment);

        // 5. CancelPayment 엔티티 생성 후 Payment와 연관 관계 설정
        CancelPayment cancelPaymentEntity = CancelPayment.fromDto(cancelPaymentFromApi.toDto());
        cancelPaymentEntity = CancelPayment.builder()
                .orderId(cancelPaymentEntity.getOrderId())
                .orderName(cancelPaymentEntity.getOrderName())
                .paymentKey(cancelPaymentEntity.getPaymentKey())
                .cancelReason(cancelRequest.getCancelReason())
                .cancelAmount(cancelRequest.getCancelAmount())
                .cancelDate(LocalDateTime.now())
                .payment(payment)
                .build();

        return cancelPaymentRepository.save(cancelPaymentEntity);
    }

    /**
     * 외부 결제 취소 API 호출
     *
     * @param paymentKey 결제 고유 키
     * @param cancelRequest 취소 요청 객체
     * @return 외부 API 응답을 기반으로 생성된 CancelPayment 엔티티
     */
    private CancelPayment requestExternalCancel(String paymentKey, CancelPaymentRequest cancelRequest) {
        String url = API_URL + paymentKey + "/cancel";
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

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("Cancel API response status: %d, body: %s%n", response.statusCode(), response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CancelPaymentResponse cancelResponse = objectMapper.readValue(response.body(), CancelPaymentResponse.class);
                return CancelPayment.fromDto(cancelResponse);
            } else {
                throw new RuntimeException("결제 취소 실패 - 상태 코드: " + response.statusCode() + ", 응답: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            System.err.printf("결제 취소 중 오류 발생: %s%n", e.getMessage());
            throw new RuntimeException("결제 취소 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * CancelPaymentRequest 객체를 JSON 문자열로 변환
     *
     * @param request 취소 요청 객체
     * @return JSON 문자열
     */
    private String convertToJson(CancelPaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류: " + e.getMessage(), e);
        }
    }

    /**
     * Basic 인증을 위한 Authorization 헤더 생성
     *
     * @return Basic Authorization 헤더 값
     */
    private String createAuthorizationHeader() {
        String authString = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        return "Basic " + encodedAuth;
    }
}
