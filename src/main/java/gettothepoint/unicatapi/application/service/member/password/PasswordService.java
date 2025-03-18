package gettothepoint.unicatapi.application.service.member.password;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final MemberService memberService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public void sendResetEmail(String email, String url) {
        try {
            Member member = memberService.findByEmail(email);
            String token = jwtUtil.generateJwtToken(member.getId(), email);
            String href = String.format("%stoken=%s", url, token);

            String content = "<h1>비밀번호 재설정</h1><p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>" +
                    "<a href=\"" + href + "\">비밀번호 재설정</a>";

            emailService.send(email, "비밀번호 재설정", content);
        } catch (ResponseStatusException e) {
            log.info("사용자가 없는 이메일로 발송 요청");
        }
    }
}
