//package taeniverse.unicatApi.mvc.controller;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.hamcrest.Matchers.containsString;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(PaymentFailController.class)
//class PaymentFailControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testHandlePaymentFail_PayProcessCanceled() throws Exception {
//        mockMvc.perform(get("/fail")
//                        .with(user("testUser").roles("USER"))
//                        .param("code", "PAY_PROCESS_CANCELED")
//                        .param("message", "사용자가 결제를 취소하였습니다"))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(containsString("결제가 구매자에 의해 취소되었습니다")));
//    }
//
//    @Test
//    void testHandlePaymentFail_PayProcessAborted() throws Exception {
//        mockMvc.perform(get("/fail")
//                        .with(user("testUser").roles("USER"))
//                        .param("code", "PAY_PROCESS_ABORTED")
//                        .param("message", "결제 처리 중 오류 발생")
//                        .param("orderId", "order-123")
//                )
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(containsString("결제 처리 중 실패가 발생하였습니다")));
//    }
//
//    @Test
//    void testHandlePaymentFail_RejectCardCompany() throws Exception {
//        mockMvc.perform(get("/fail")
//                        .with(user("testUser").roles("USER"))
//                        .param("code", "REJECT_CARD_COMPANY")
//                        .param("message", "카드사에서 결제를 거절하였습니다")
//                        .param("orderId", "order-456")
//                )
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string(containsString("입력하신 카드 정보에 문제가 있습니다")));
//    }
//
//    @Test
//    void testHandlePaymentFail_OtherError() throws Exception {
//        mockMvc.perform(get("/fail")
//                        .with(user("testUser").roles("USER"))
//                        .param("code", "UNKNOWN_ERROR")
//                        .param("message", "알 수 없는 오류 발생")
//                        .param("orderId", "order-789")
//                )
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string(containsString("알 수 없는 결제 오류가 발생하였습니다")));
//    }
//}