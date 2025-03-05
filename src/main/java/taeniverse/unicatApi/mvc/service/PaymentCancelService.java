package taeniverse.unicatApi.mvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.component.propertie.AppProperties;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentRequest;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private static final String API_URL = "https://api.tosspayments.com/v1/payments/";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

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
    public CancelPaymentResponse cancelPayment(String paymentKey, CancelPaymentRequest cancelRequest) {
//        // 1. 기존 Payment 조회
//        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
//                .orElseThrow(() -> new RuntimeException("Payment not found with key: " + paymentKey));
//
//        // 2. 입력값 검증
//        if (cancelRequest.getCancelReason() == null || cancelRequest.getCancelReason().trim().isEmpty()) {
//            throw new RuntimeException("취소 사유는 필수입니다.");
//        }
//
//        // 3. Toss API 결제 취소 요청 및 응답 받기 (이제 `CancelPaymentResponse`가 반환됨)
//        CancelPaymentResponse cancelPaymentResponse = requestExternalCancel(paymentKey, cancelRequest);
//
//        // 4. Toss API 응답에서 `status`와 `method` 값 변환
//        TossPaymentStatus tossPaymentStatus = cancelPaymentResponse.getTossPaymentStatus();
//        PayType payType = cancelPaymentResponse.getPayType(); // ✅ `method` 값을 PayType Enum으로 변환
//
//        // 5. 기존 Payment 엔티티 상태 업데이트 및 저장
//        payment.setTossPaymentStatus(tossPaymentStatus); // ✅ Toss API 응답 반영
//        payment.setPayType(payType); // ✅ 결제 수단 반영
//        paymentRepository.save(payment); // 변경사항 저장
//
//        // 6. CancelPayment 엔티티 생성 후 Payment와 연관 관계 설정
//        CancelPayment cancelPaymentEntity = CancelPayment.builder()
//                .orderId(payment.getOrder().getOrderId())
//                .orderName(payment.getOrder().getOrderName())
//                .paymentKey(payment.getPaymentKey())
//                .cancelReason(cancelRequest.getCancelReason())
//                .cancelAmount(cancelRequest.getCancelAmount())
//                .cancelDate(LocalDateTime.now())
//                .status(tossPaymentStatus)
//                .method(payType)
//                .payment(payment)
//                .build();
//
//        cancelPaymentRepository.save(cancelPaymentEntity);
//
//        // 7. CancelPaymentResponse DTO 반환
//        return cancelPaymentResponse;
        return null;
    }

    /**
     * 외부 결제 취소 API 호출
     *
     * @param paymentKey 결제 고유 키
     * @param cancelRequest 취소 요청 객체
     * @return 외부 API 응답을 기반으로 생성된 CancelPayment 엔티티
     */
    private CancelPaymentResponse requestExternalCancel(String paymentKey, CancelPaymentRequest cancelRequest) {
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
                return objectMapper.readValue(response.body(), CancelPaymentResponse.class); // ✅ `CancelPaymentResponse` 반환
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
        String authString = appProperties.toss().secretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        return "Basic " + encodedAuth;
    }
}
