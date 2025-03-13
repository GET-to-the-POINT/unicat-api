package gettothepoint.unicatapi.application.service.password;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.common.util.UrlUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final MemberService memberService;
    private final JavaMailSender emailSender;
    private final AppProperties appProperties;
    private final JwtUtil jwtUtil;


    public void sendPasswordResetEmail(String email) {
        // 회원 존재 여부 검증 등 필요 로직 추가
        String resetUrl = createResetLink(email);
        String title = "비밀번호 재설정 이메일입니다.";
        String content = String.format(
                "<html>" +
                        "<body>" +
                        "<h1>비밀번호 재설정 요청</h1>" +
                        "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>" +
                        "<a href=\"%s\">비밀번호 재설정하기</a>" +
                        "</body>" +
                        "</html>", resetUrl);
        sendEmail(email, title, content);
    }
    public void sendEmail(String toEmail, String title, String content) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(content, true);
            emailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email", e);
        }
    }

    public String createResetLink(String email) {
       Member member = memberService.findByEmail(email);
       String token = jwtUtil.generateJwtToken(member.getId(),email);
       String baseUrl = UrlUtil.buildBaseUrl(appProperties.api());
       return String.format("%s/reset-password?token=%s", baseUrl, token);
    }

    public void resetPassword(String email, String newPassword, String newPasswordConfirmation) {
        if (!newPassword.equals(newPasswordConfirmation)) {
            throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
        }
        if (memberService.findByEmail(email) == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }
        memberService.updatePassword(email, newPassword);
    }
}
