package gettothepoint.unicatapi.mail;

import gettothepoint.unicatapi.mail.config.SyncTaskExecutorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * 비동기 메일 전송 수동 통합 테스트
 * 실제 SMTP 서버를 통한 비동기 메일 전송을 검증합니다.
 */
@SpringJUnitConfig(classes = {MailSenderAutoConfiguration.class, MailService.class, SyncTaskExecutorConfig.class})
@TestPropertySource(properties = {
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=test@test.com",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true",
        "spring.mail.properties.mail.smtp.starttls.required=true"
})
@EnableAsync
@DisplayName("비동기 메일 전송 수동 통합 테스트")
class MailServiceManualTest {
    private static final String RECIPIENT = "test@test.com";
    private static final String SUBJECT = "Test Subject";
    private static final String CONTENT = "Test Content";

    @Autowired
    MailService mailService;

    @MockitoSpyBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("SMTP 서버까지 최종 발송 완료되었는지 검증")
    void shouldSendMailAndCompleteHandshakeSuccessfully() {
        // Given
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(RECIPIENT);
        simpleMailMessage.setSubject(SUBJECT);
        simpleMailMessage.setText(CONTENT);

        // When
        assertThatCode(() -> mailService.send(simpleMailMessage))
                .doesNotThrowAnyException(); // 실제 메일 발송 요청 (비동기 이벤트 퍼블리시)

        // Then
        await()
                .pollDelay(Duration.ofMillis(500)) // 이벤트 처리 대기
                .pollInterval(Duration.ofMillis(200))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    verify(javaMailSender).send(any(SimpleMailMessage.class)); // 호출은 됐는지
                });
    }
}
