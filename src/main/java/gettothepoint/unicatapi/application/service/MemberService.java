package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.domain.entity.Member;
import gettothepoint.unicatapi.domain.entity.OAuthLink;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.OAuthLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Member validateCredentials(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return member;
    }
}
