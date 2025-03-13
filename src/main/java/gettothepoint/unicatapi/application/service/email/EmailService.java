package gettothepoint.unicatapi.application.service.email;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
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

            setHelperFrom(helper);

            emailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email", e);
        }
    }

    private void setHelperFrom(MimeMessageHelper helper) throws MessagingException {
        String from = appProperties.email().from();
        if (from == null) {
            return;
        }
        String fromName = appProperties.email().fromName();
        helper.setFrom(resolveFromAddress(from, fromName));
    }

    private InternetAddress resolveFromAddress(String from, String fromName) {
        try {
            return new InternetAddress(from, fromName);
        } catch (UnsupportedEncodingException e) {
            try {
                return new InternetAddress(from);
            } catch (AddressException ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to resolve from address", ex);
            }
        }
    }

    public String generateVerificationLink(String email) {
        String baseUrl = UrlUtil.buildBaseUrl(appProperties.api());
        return String.format("%s/email/verifyEmail?email=%s", baseUrl, URLEncoder.encode(email, StandardCharsets.UTF_8));
    }

    public void sendVerificationEmail(String email) {
        String verifyUrl = generateVerificationLink(email);
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