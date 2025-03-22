package gettothepoint.unicatapi.application.service.email;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.common.util.UrlUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;
    private final AppProperties appProperties;
    private final JwtUtil jwtUtil;

    public void send(String recipient, String subject, String content) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setFrom(new InternetAddress(appProperties.email().from(), appProperties.email().fromName()));

            emailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(Member member) {
        String verificationLink = String.format("%s/members/email?token=%s",
                UrlUtil.buildBaseUrl(appProperties.api()),
                URLEncoder.encode(jwtUtil.generateJwtToken(member.getId(), member.getEmail()), StandardCharsets.UTF_8));

        String content = "<h1>이메일 인증</h1><p>아래 링크를 클릭하여 이메일을 인증하세요.</p>" +
                "<a href=\"" + verificationLink + "\">이메일 인증하기</a>";

        send(member.getEmail(), "회원가입 인증", content);
    }
}
