package taeniverse.unicatApi.mvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;
import taeniverse.unicatApi.mvc.model.entity.Role;
import taeniverse.unicatApi.mvc.repository.MemberRepository;
import taeniverse.unicatApi.mvc.repository.OAuth2Repository;
import taeniverse.unicatApi.mvc.repository.RoleRepository;
import taeniverse.unicatApi.temp.CustomOAuth2User;
import taeniverse.unicatApi.temp.OAuth2UserInfo;
import taeniverse.unicatApi.temp.OAuth2UserInfoFactory;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final OAuth2Repository oauth2Repository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        var attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email", "Email not found from OAuth2 provider", "")); // TODO 예외를 좀 더  정교하게 변경
        }

        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .email(userInfo.getEmail())
                            .password("{noop}oauth2user") // TODO 좀 더 정교한 방식으로 변경하기
                            .build();

                    memberRepository.save(newMember);

                    Role role = Role.builder()
                            .role("ROLE_USER")
                            .member(newMember)
                            .build();

                    roleRepository.save(role);

                    return newMember;
                });

        OAuth2 oauth2 = oauth2Repository.findByUsername(userInfo.getId())
                .orElseGet(() -> {
                    OAuth2 newOAuth2 = OAuth2.builder()
                            .provider(registrationId)
                            .username(userInfo.getId())
                            .member(member)
                            .email(userInfo.getEmail())
                            .build();
                    return oauth2Repository.save(newOAuth2);
                });

        return new CustomOAuth2User(member, oauth2, attributes);
    }
}