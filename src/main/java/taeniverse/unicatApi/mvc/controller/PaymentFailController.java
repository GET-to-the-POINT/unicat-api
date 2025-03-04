//package taeniverse.unicatApi.mvc.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/fail")
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentFailController {
//    @GetMapping
//    public ResponseEntity<?> handlePaymentFail(
//            @RequestParam(required = false) String code,
//            @RequestParam(required = false) String message,
//            @RequestParam(required = false) String orderId) {
//
//        log.info("Payment failed - code: {}, message: {}, orderId: {}", code, message, orderId);
//
//        // 각 오류 코드에 따른 추가 로직 구현 가능
//        if ("PAY_PROCESS_CANCELED".equals(code)) {
//            // 구매자가 결제를 취소한 경우 (orderId가 없을 수 있음)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("결제가 구매자에 의해 취소되었습니다: " + message);
//        } else if ("PAY_PROCESS_ABORTED".equals(code)) {
//            // 결제 실패로 인한 경우
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("결제 처리 중 실패가 발생하였습니다: " + message);
//        } else if ("REJECT_CARD_COMPANY".equals(code)) {
//            // 카드사에서 결제 거절한 경우
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("입력하신 카드 정보에 문제가 있습니다: " + message);
//        } else {
//            // 기타 오류 처리
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("알 수 없는 결제 오류가 발생하였습니다: " + message);
//        }
//    }
//}