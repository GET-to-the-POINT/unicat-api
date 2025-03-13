package gettothepoint.unicatapi.application.service.email;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;
    private final AppProperties appProperties;

    public void sendEmail(String toEmail, String title, String content) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(content, true);
            helper.setReplyTo(appProperties.email().replyTo());

            emailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error(e.getMessage(), e);
        }
    }
}
