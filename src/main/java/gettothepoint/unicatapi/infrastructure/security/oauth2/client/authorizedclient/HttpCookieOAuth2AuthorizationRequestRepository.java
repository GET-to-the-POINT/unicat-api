package gettothepoint.unicatapi.infrastructure.security.oauth2.client.authorizedclient;

import gettothepoint.unicatapi.common.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST = HttpCookieOAuth2AuthorizationRequestRepository.class.getName() + "oauth2_auth_request";
    public static final String REDIRECT = "redirect";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST);
        if (cookie == null) {
            return null;
        }
        return deserialize(cookie.getValue());
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            Cookie oauth2AuthRequestCookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST);
            assert oauth2AuthRequestCookie != null;
            CookieUtil.fire(oauth2AuthRequestCookie);

            Cookie redirectCookie = WebUtils.getCookie(request, REDIRECT);
            assert redirectCookie != null;
            CookieUtil.fire(redirectCookie);
            return;
        }
        String serializedAuthRequest = serialize(authorizationRequest);
        Cookie jwtCookie = CookieUtil.create(OAUTH2_AUTH_REQUEST, serializedAuthRequest, COOKIE_EXPIRE_SECONDS);
        response.addCookie(jwtCookie);

        String redirectUriAfterSignIn = request.getParameter(REDIRECT);
        if (redirectUriAfterSignIn != null && !redirectUriAfterSignIn.isEmpty()) {
            Cookie cookie = CookieUtil.create(REDIRECT, redirectUriAfterSignIn, COOKIE_EXPIRE_SECONDS);
            response.addCookie(cookie);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        Cookie oauth2AuthRequestCookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST);
        assert oauth2AuthRequestCookie != null;
        CookieUtil.fire(oauth2AuthRequestCookie);
        return authorizationRequest;
    }

    private String serialize(OAuth2AuthorizationRequest object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String cookieValue) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getUrlDecoder().decode(cookieValue));
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (OAuth2AuthorizationRequest) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to deserialize OAuth2AuthorizationRequest", e);
        }
    }

}