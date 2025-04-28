package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.config.SyncTaskExecutorTestConfig;
import gettothepoint.unicatapi.email.config.AsyncMailSenderTestConfig;
import gettothepoint.unicatapi.email.config.MailContainer;
import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import gettothepoint.unicatapi.email.infrastructure.config.AsyncConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 비동기 메일 전송 통합 테스트
 * Mailpit 컨테이너를 사용해 비동기 메일 전송과 수신을 검증합니다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    AsyncMailSenderTestConfig.class,
    AsyncConfig.class,
    SyncTaskExecutorTestConfig.class
})
@Testcontainers
@DisplayName("비동기 메일 전송 통합 테스트")
class AsyncMailSenderIntegrationTest extends AbstractMailSenderTest {

    @Container
    static MailContainer mailpit = new MailContainer("axllent/mailpit");

    @DynamicPropertySource
    static void overrideMailProps(DynamicPropertyRegistry registry) {
        mailpit.start();
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", mailpit::getSmtpPort);
    }

    @Autowired
    MailSender mailSender;

    @Test
    @DisplayName("비동기 메일이 올바르게 전송되고 수신되는지 검증한다")
    void shouldSendMailAsynchronouslyAndVerifyDelivery() {
        // 테스트용 메일 메시지 생성
        MailMessage mailMessage = createTestMailMessage();

        // 메일 전송
        mailSender.send(mailMessage);
        logMailSent();

        // 메일 수신 확인
        verifyMailDelivery(mailpit);
    }
}
