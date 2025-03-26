package gettothepoint.unicatapi.infrastructure.security.oauth2.client;

import gettothepoint.unicatapi.common.util.CookieUtil;
import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.infrastructure.security.oauth2.client.authorizedclient.HttpCookieOAuth2AuthorizationRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Long memberId = oAuth2User.getAttribute("memberId");
        assert memberId != null;
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        Subscription subscription = member.getSubscription();
        String email = oAuth2User.getAttribute("email");

        String token = jwtUtil.generateJwtToken(memberId, email, subscription.getPlan().getName());
        Cookie jwtCookie = cookieUtil.createJwtCookie(token);
        response.addCookie(jwtCookie);

        // 꺼낸뒤 버린다.
        Cookie redirectCookie = WebUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT);
        String redirectUrl = "/";
        if (redirectCookie != null) {
            redirectUrl = redirectCookie.getValue();
            cookieUtil.zeroAge(redirectCookie);
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}