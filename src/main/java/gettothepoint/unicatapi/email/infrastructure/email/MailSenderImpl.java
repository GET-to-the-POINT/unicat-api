package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import gettothepoint.unicatapi.email.domain.exception.MailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderImpl implements MailSender {

    private final JavaMailSender javaMailSender;

    @Override
    public void send(MailMessage mailMessage) throws MailSendException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(mailMessage.recipient());
            helper.setSubject(mailMessage.subject());
            helper.setText(mailMessage.content(), mailMessage.isHtml());

            javaMailSender.send(message);
            
            log.info("이메일 전송 완료: {} - {}", mailMessage.recipient(), mailMessage.subject());
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
            throw new MailSendException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }
}
