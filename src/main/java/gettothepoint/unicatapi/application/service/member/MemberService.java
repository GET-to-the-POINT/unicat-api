package gettothepoint.unicatapi.application.service.member;

import gettothepoint.unicatapi.application.service.payment.SubscriptionService;
import gettothepoint.unicatapi.domain.dto.member.MemberUpdateDto;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.member.OAuthLink;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.OAuthLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthLinkRepository oAuthLinkRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;


    @Transactional
    public Member create(String email, String password, String name, String phoneNumber) {
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .phoneNumber(phoneNumber)
                .build();

        subscriptionService.createSubscription(member);
        return memberRepository.save(member);
    }

    public Member getOrElseThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found with id: " + memberId));
    }

    public Member getOrElseThrow(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No user found with email: " + email));
    }

    @Transactional
    public Member findOrCreateMember(String email, String registrationId) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> {

                    Member newMember = Member.builder()
                            .email(email)
                            .password("{noop}oauth2user")
                            .build();
                    newMember.verified();

                    Member savedMember = memberRepository.save(newMember);

                    oAuthLinkRepository.save(
                            OAuthLink.builder()
                                    .email(email)
                                    .provider(registrationId)
                                    .member(savedMember)
                                    .build()
                    );

                    subscriptionService.createSubscription(savedMember);

                    return savedMember;
                });
    }

    public boolean isEmailTaken(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public boolean validCurrentPassword(Long memberId, String currentPassword) {
        Member member = getOrElseThrow(memberId);
        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    public void updatePassword(Long memberId, String newPassword) {
        Member member = getOrElseThrow(memberId);
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }


    public void update(Member member) {
        memberRepository.save(member);
    }

    public void updateMember(Long memberId, MemberUpdateDto dto) {
        Member member = getOrElseThrow(memberId);
        member.setName(dto.name());
        member.setPhoneNumber(dto.phoneNumber());
        memberRepository.save(member);
    }
    public void verifyEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member not found"));

        member.verified();
        memberRepository.save(member);
    }
}


