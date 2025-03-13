package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.email.EmailService;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.UrlUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.member.OAuthLink;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.OAuthLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthLinkRepository oAuthLinkRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

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

    public String generateVerificationLink(String email) {
        return UrlUtil.buildBaseUrl(appProperties.api()) +"/email/verifyEmail?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
    }

    public void sendVerificationEmail(String email) {
        String verifyUrl = generateVerificationLink(email);
        String title = "Unicat 회원 가입 인증 이메일입니다.";
        String content = "<html>" +
                "<body>" +
                "<h1>Unicat 인증 이메일입니다.</h1>" +
                "<p>아래 링크를 클릭하시면 회원 인증이 완료됩니다.</p>" +
                "<a href=\"" + verifyUrl + "\">회원 인증하기</a>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(email, title, content);
    }

    public void verifyEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member not found"));

        member.verified();
        memberRepository.save(member);
    }
}


