package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static gettothepoint.unicatapi.email.config.CommonConfig.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("manual")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MailSenderAutoConfiguration.class, SyncMailSender.class})
@TestPropertySource(properties = {
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=test@test.com",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true",
        "spring.mail.properties.mail.smtp.starttls.required=true",
        "spring.mail.properties.mail.smtp.connectiontimeout=20000",
        "spring.mail.properties.mail.smtp.timeout=20000",
        "spring.mail.properties.mail.smtp.writetimeout=20000"
})
class SyncMailSenderManualIntegrationTest {

    @MockitoSpyBean
    MailSender mailSender;

    @Test
    void send() {
        MailMessage mailMessage = MailMessage.builder()
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .content(CONTENT)
                .isHtml(false)
                .build();
        mailSender.send(mailMessage);

        await().atMost(5, SECONDS).untilAsserted(() ->
                verify(mailSender, times(1)).send(Mockito.eq(mailMessage)));
    }
}
