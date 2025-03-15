package gettothepoint.unicatapi.application.service.password;

import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.domain.entity.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final MemberService memberService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void sendPasswordResetEmail(String email) {
        Member member = memberService.findByEmail(email);
        emailService.sendPasswordResetEmail(email, member.getId());
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String newPasswordConfirmation) {
        if (!newPassword.equals(newPasswordConfirmation)) {
            throw new IllegalArgumentException("새 비밀번호와 확인이 일치하지 않습니다.");
        }
        memberService.updatePassword(email, newPassword);
    }

    public boolean verifyCurrentPassword(String email, String currentPassword) {
        Member member = memberService.findByEmail(email);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }
}
