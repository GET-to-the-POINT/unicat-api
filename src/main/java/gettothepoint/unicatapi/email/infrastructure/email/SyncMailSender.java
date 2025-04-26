package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(MailMessage message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
        try {
            helper.setTo(message.recipient());
            helper.setSubject(message.subject());
            helper.setText(message.content(), message.isHtml());

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailSendException("메일 전송 실패", e);
        }
    }
}
