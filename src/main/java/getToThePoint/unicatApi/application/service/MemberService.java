package getToThePoint.unicatApi.application.service;

import getToThePoint.unicatApi.domain.entity.Member;
import getToThePoint.unicatApi.domain.entity.OAuthLink;
import getToThePoint.unicatApi.domain.repository.MemberRepository;
import getToThePoint.unicatApi.domain.repository.OAuthLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthLinkRepository oAuthLinkRepository;

    public Member create(String email, String password) {
        Member member = Member.builder().email(email)
                .password(password)
                .build();

        return memberRepository.save(member);
    }
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 이메일을 가진 사용자가 없습니다: " + email));
    }

    public Member findOrCreateMember(String email, String registrationId) {
        return memberRepository.findByEmail(email).orElseGet(() -> {
            Member newMember = Member.builder()
                    .email(email)
                    .password("{noop}oauth2user")  // 추후 변경 가능
                    .build();
            memberRepository.save(newMember);

            // OAuthLink 생성
            OAuthLink oAuthLink = OAuthLink.builder()
                    .email(email)
                    .provider(registrationId)
                    .member(newMember)
                    .build();
            oAuthLinkRepository.save(oAuthLink);

            return newMember;
        });
    }

}