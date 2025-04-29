package gettothepoint.unicatapi.member.application;

import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.mail.MailService;
import gettothepoint.unicatapi.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final MemberService memberService;
    private final MailService mailService;
    private final JwtUtil jwtUtil;

    public void sendResetEmail(String email, String url) {
        try {
            Member member = memberService.getOrElseThrow(email);
            String token = jwtUtil.generateJwtToken(member.getId());
            String href = String.format("%stoken=%s", url, token);

            String content = "<h1>비밀번호 재설정</h1><p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>" +
                    "<a href=\"" + href + "\">비밀번호 재설정</a>";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("비밀번호 재설정");
            message.setText(content);

            mailService.send(message);
        } catch (ResponseStatusException e) {
            log.info("사용자가 없는 이메일로 발송 요청");
        }
    }
}
