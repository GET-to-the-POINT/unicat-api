package taeniverse.ai_news.mvc.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import taeniverse.ai_news.config.JWTUtil;
import taeniverse.ai_news.mvc.model.dto.OAuthDTO;
import taeniverse.ai_news.mvc.model.dto.PrincipalDetails;
import taeniverse.ai_news.mvc.model.dto.SignDTO;
import taeniverse.ai_news.mvc.service.UserService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SignController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Value("${ai-news.api.protocol}")
    private String backProtocol;
    @Value("${ai-news.api.domain}")
    private String backDomain;
    @Value("${ai-news.api.port}")
    private String backPort;

    public SignController(UserService userService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody SignDTO signDTO) {
        userService.signUp(signDTO);
    }


    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void authenticate(@RequestBody SignDTO signDTO, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signDTO.getEmail(), signDTO.getPassword()));
            PrincipalDetails userDetails = (PrincipalDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority authority = iterator.next();

            String role = authority.getAuthority();

            jwtUtil.setJwtResponse(response, email, userDetails.getUserId(), role);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호나 아이디가 일치하지 않습니다.");
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public String example(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return "Authorization header: " + authorization;
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/oauth2")
    public List<Map<String, String>> oauth2() {
        return List.of(createOAuth2ProviderMap("google", "#4285F4"));
    }

    private Map<String, String> createOAuth2ProviderMap(String provider, String backgroundColor) {
        final String backUrl = String.join("", backProtocol, backDomain, ":", backPort, "/oauth2/authorization/", provider);
        return Map.of("provider", provider, "url", backUrl, "backgroundColor", backgroundColor);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/sign-out")
    public void signOut(HttpServletResponse response) {
        this.jwtUtil.deleteJwtResponse(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-info")
    public OAuthDTO userInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            return principalDetails.user();
        } catch (Exception e) {
            return null;
        }
    }
}

