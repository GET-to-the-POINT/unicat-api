package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.config.MailSenderTestConfig;
import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("manual")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MailSenderAutoConfiguration.class, MailSenderImpl.class})
@TestPropertySource(properties = {
        "spring.mail.host=smtp.gmail.com",
        "spring.mail.port=587",
        "spring.mail.username=test@gmail.com",
        "spring.mail.password=test",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=true",
        "spring.mail.properties.mail.smtp.starttls.required=true",
        "spring.mail.properties.mail.smtp.connectiontimeout=20000",
        "spring.mail.properties.mail.smtp.timeout=20000",
        "spring.mail.properties.mail.smtp.writetimeout=20000"
})
class MailSenderImplManualIntegrationTest {

    @Autowired
    private MailSender mailSender;

    @Test
    void sendMailWithRealSmtp() {
        MailMessage mailMessage = MailMessage.builder()
                .recipient(MailSenderTestConfig.RECIPIENT)
                .subject(MailSenderTestConfig.SUBJECT)
                .content(MailSenderTestConfig.CONTENT)
                .isHtml(false)
                .build();

        mailSender.send(mailMessage);
        System.out.println("실제 SMTP 서버를 통해 메일이 전송되었습니다. 받은 편지함을 확인해주세요.");
    }
}
