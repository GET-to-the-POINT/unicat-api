package gettothepoint.unicatapi.application.service.member;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.member.OAuthLink;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.OAuthLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthLinkRepository oAuthLinkRepository;
    private final PasswordEncoder passwordEncoder;

    public Member create(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }
        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();
        return memberRepository.save(member);
    }

    public Member findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with email: " + email));
    }

    public Member findOrCreateMember(String email, String registrationId) {
        if (email == null || registrationId == null) {
            throw new IllegalArgumentException("Email and registrationId must not be null");
        }
        return memberRepository.findByEmail(email).orElseGet(() -> {
            Member newMember = Member.builder()
                    .email(email)
                    .password("{noop}oauth2user")
                    .build();
            memberRepository.save(newMember);

            OAuthLink oAuthLink = OAuthLink.builder()
                    .email(email)
                    .provider(registrationId)
                    .member(newMember)
                    .build();
            oAuthLinkRepository.save(oAuthLink);

            return newMember;
        });
    }

    public boolean isEmailTaken(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public void verifyEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member not found"));

        member.verified();
        memberRepository.save(member);
    }

    public boolean validCurrentPassword(String email, String currentPassword) {
        Member member = findByEmail(email);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    public void updatePassword(String email, String newPassword) {
        Member member = findByEmail(email);
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    public Member getOrElseThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found with id: " + memberId));
    }
}


