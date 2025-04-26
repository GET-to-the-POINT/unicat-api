package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.config.SyncTaskExecutorTestConfig;
import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * 비동기 메일 전송 수동 통합 테스트
 * 실제 SMTP 서버를 통한 비동기 메일 전송을 검증합니다.
 * 주의: 실행하려면 @Tag("manual")를 주석 처리해야 합니다.
 */
@Tag("manual") // 이 테스트는 이 태그를 주석 처리하고 테스트를 실행해야합니다.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MailSenderAutoConfiguration.class, AsyncMailSender.class, SyncMailSender.class, MailEventListener.class, SyncTaskExecutorTestConfig.class})
@TestPropertySource(properties = {
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=test@test.com",
        "spring.mail.password=testpassword",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true",
        "spring.mail.properties.mail.smtp.starttls.required=true",
        "spring.mail.properties.mail.smtp.connectiontimeout=20000",
        "spring.mail.properties.mail.smtp.timeout=20000",
        "spring.mail.properties.mail.smtp.writetimeout=20000"
})
@EnableAsync
@DisplayName("비동기 메일 전송 수동 통합 테스트")
class AsyncMailSenderManualIntegrationTest extends AbstractMailSenderTest {

    @Autowired
    MailSender mailSender;

    @MockitoSpyBean
    SyncMailSender syncSender;

    @MockitoSpyBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("실제 SMTP 서버로 비동기 메일 전송 시 예외가 발생하지 않는다")
    void shouldSendMailAsynchronouslyWithoutException() {
        // 테스트용 메일 메시지 생성
        MailMessage mailMessage = createTestMailMessage();
        
        // 예외 없이 메일이 전송되는지 확인
        assertDoesNotThrow(() -> mailSender.send(mailMessage), "메일 전송 시 예외가 발생했습니다");
    }
}
