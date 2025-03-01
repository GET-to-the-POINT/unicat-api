package taeniverse.unicatApi.mvc.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import taeniverse.unicatApi.mvc.model.dto.sign.SignInDto;
import taeniverse.unicatApi.mvc.model.dto.sign.SignUpDto;
import taeniverse.unicatApi.mvc.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Sign API", description = "회원가입, 사인인, 사인아웃 관련 API")
public class SignController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입 (Sign-Up)",
            description = "새로운 사용자를 등록하고 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
            }
    )
    public void signUp(@Valid @RequestBody SignUpDto signUpDto, HttpServletResponse response) {
        authService.signUp(signUpDto, response);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "사인인 (Sign-In)",
            description = "사용자 인증 후 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사인인 성공"),
                    @ApiResponse(responseCode = "401", description = "잘못된 이메일 또는 비밀번호")
            }
    )
    public void signIn(@Valid @RequestBody SignInDto signInDto, HttpServletResponse response) {
        authService.signIn(signInDto, response);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "사인아웃 (Sign-Out)",
            description = "JWT 토큰 쿠키를 제거하여 로그아웃합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사인아웃 성공")
            }
    )
    public void signOut(HttpServletResponse response) {
        authService.signOut(response);
    }
}