package taeniverse.unicatApi.mvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import taeniverse.unicatApi.config.JWTUtil;
import taeniverse.unicatApi.mvc.model.dto.PrincipalDetails;
import taeniverse.unicatApi.mvc.model.dto.SignDTO;
import taeniverse.unicatApi.mvc.service.UserService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Tag(name = "인증 API", description = """
**인증은 JWT를 기반으로 합니다.**

사인인하면 JWT 쿠키가 발급됩니다. \n
헤더를 통한 인증도 가능하지만, **브라우저 환경에서는 쿠키 기반 인증을 추천합니다.**

#### 사용 방법
1. `/etc/hosts` 파일의 마지막에 다음 내용을 추가합니다.
    127.0.0.1 unicat.day
2. 공유받은 인증서를 사용하여 `webui` 서버를 실행합니다.
3. 이후 **쿠키 기반 인증이 활성화**됩니다.
""")
@RestController
@RequestMapping("/api")
public class SignController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @Value("${app.api.protocol}")
    private String apiProtocol;
    @Value("${app.api.domain}")
    private String apiDomain;
    @Value("${app.api.port}")
    private String apiPort;

    public SignController(UserService userService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "사인 업(테스트 용도)", description = "이메일과 비밀번호로 새로운 계정을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody SignDTO signDTO) {
        userService.signUp(signDTO);
    }

    @Operation(
            summary = "사인인",
            description = "이메일과 비밀번호로 사인인을 시도합니다. 성공하면 JWT 토큰을 응답 헤더 및 쿠키에 설정합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "사인인 성공, JWT 토큰 반환 (헤더, 쿠키)"),
                    @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 오류")
            }
    )
    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void authenticate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사인인 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignDTO.class))
            )
            @RequestBody SignDTO signDTO,
            HttpServletResponse response) {
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

    @Operation(summary = "사인아웃", description = "JWT 토큰을 삭제하여 사인아웃합니다.")
    @ApiResponse(responseCode = "200", description = "사인아웃 성공")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/sign-out")
    public void signOut(HttpServletResponse response) {
        this.jwtUtil.deleteJwtResponse(response);
    }

    @Operation(summary = "OAuth2 사인인 제공자 조회", description = "지원하는 OAuth2 사인인 제공자의 목록을 반환합니다.")
    @PreAuthorize("isAnonymous()")
    @GetMapping("/oauth2s")
    public List<Map<String, String>> oauth2() {
        return List.of(createOAuth2ProviderMap("google", "#4285F4"));
    }

    private Map<String, String> createOAuth2ProviderMap(String provider, String backgroundColor) {
        String apiUrl = apiProtocol + "://" + apiDomain + ":" + apiPort + "/oauth2/authorization/" + provider;
        return Map.of("provider", provider, "url", apiUrl, "backgroundColor", backgroundColor);
    }
}