package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 동기 메일 전송 수동 통합 테스트
 * 실제 SMTP 서버를 통한 메일 전송을 검증합니다.
 * 주의: 실행하려면 @Tag("manual")를 주석 처리해야 합니다.
 */
@Tag("manual") // 이 테스트는 이 태그를 주석 처리하고 테스트를 실행해야합니다.
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MailSenderAutoConfiguration.class, SyncMailSender.class})
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
@DisplayName("동기 메일 전송 수동 통합 테스트")
class SyncMailSenderManualIntegrationTest extends AbstractMailSenderTest {

    @MockitoSpyBean
    MailSender mailSender;

    @Test
    @DisplayName("실제 SMTP 서버로 동기 메일이 한 번 전송되는지 검증한다")
    void shouldSendMailSynchronouslyAndVerifyCall() {
        // 테스트용 메일 메시지 생성
        MailMessage mailMessage = createTestMailMessage();
        
        // 메일 전송
        mailSender.send(mailMessage);

        // 메일 전송 메서드가 한 번 호출되었는지 검증
        await().atMost(5, SECONDS).untilAsserted(() ->
                verify(mailSender, times(1)).send(Mockito.eq(mailMessage)));
    }
}
