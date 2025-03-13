package gettothepoint.unicatapi.application.service.password;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final MemberService memberService;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;


    public void sendPasswordResetEmail(String email) {
        memberService.findByEmail(email);

        String resetLink = createResetLink(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("비밀번호 재설정 링크");
        message.setText("아래 링크를 클릭하여 비밀번호를 재설정하세요:\n" + resetLink);

        mailSender.send(message);
    }

    public String createResetLink(String email) {
        String baseUrl = UrlUtil.buildBaseUrl(appProperties.api());
        return String.format("%s/reset-password?email=%s", baseUrl, URLEncoder.encode(email, StandardCharsets.UTF_8));
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
