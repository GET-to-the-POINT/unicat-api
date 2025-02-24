package taeniverse.unicatApi.mvc.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.constant.Role;
import taeniverse.unicatApi.mvc.model.dto.*;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;
import taeniverse.unicatApi.mvc.model.entity.User;
import taeniverse.unicatApi.mvc.repository.OAuth2Repository;
import taeniverse.unicatApi.mvc.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2Repository OAuth2Repository;

    public CustomOAuth2UserService(UserRepository userRepository, OAuth2Repository oAuth2Repository) {
        this.userRepository = userRepository;
        this.OAuth2Repository = oAuth2Repository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;

        switch (registrationId) {
            case "google" -> oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
            case "naver" -> oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
            case "kakao" -> oAuth2Response = new KakkoResponse(oAuth2User.getAttributes());
            default -> {
                return null;
            }
        }

        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        OAuth2 existData = OAuth2Repository.findByUsername(username);

        if (existData == null) {
            User user = User.builder()
                            .email(oAuth2Response.getEmail())
                            .role(Role.USER.name())
                            .password("oauth2")
                            .build();

            userRepository.save(user);

            OAuth2 oAuth2 = OAuth2.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .user(user)
                    .build();

            OAuth2Repository.save(oAuth2);

            OAuthDTO authDTO = OAuthDTO.builder()
                    .userId(user.getId())
                    .username(username)
                    .name(oAuth2Response.getName())
                    .role(Role.USER.name())
                    .build();

            return new PrincipalDetails(authDTO);

        } else {

            existData.setEmail(oAuth2Response.getEmail());

            OAuth2Repository.save(existData);

            OAuthDTO authDTO = OAuthDTO.builder()
                    .userId(existData.getUser().getId())
                    .username(username)
                    .name(oAuth2Response.getName())
                    .role(Role.USER.name())
                    .build();

            return new PrincipalDetails(authDTO);

        }
    }
}
