package gettothepoint.unicatapi.application.service.email;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;
    private final AppProperties appProperties;

    public void sendEmail(String toEmail, String title, String content) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(content, true);
            helper.setReplyTo(appProperties.email().replyTo());
            emailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
    public String createVerificationLink(String email) {
        String baseUrl = UrlUtil.buildBaseUrl(appProperties.api());
        return String.format("%s/email/verifyEmail?email=%s", baseUrl, URLEncoder.encode(email, StandardCharsets.UTF_8));
    }

    public void sendVerificationEmail(String email) {
        String verifyUrl = createVerificationLink(email);
        String title = "Unicat 회원 가입 인증 이메일입니다.";
        String content = String.format(
                "<html>" +
                        "<body>" +
                        "<h1>Unicat 인증 이메일입니다.</h1>" +
                        "<p>아래 링크를 클릭하시면 회원 인증이 완료됩니다.</p>" +
                        "<a href=\"%s\">회원 인증하기</a>" +
                        "</body>" +
                        "</html>", verifyUrl);
        sendEmail(email, title, content);
    }
}