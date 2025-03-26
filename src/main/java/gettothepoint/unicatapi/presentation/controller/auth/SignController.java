package gettothepoint.unicatapi.presentation.controller.auth;

import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.domain.dto.sign.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.common.schema.ErrorResponse;
import gettothepoint.unicatapi.common.schema.UnauthorizedErrorResponse;
import gettothepoint.unicatapi.domain.dto.sign.SignInRequest;
import gettothepoint.unicatapi.application.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth - Sign API", description = "멤버 관련 api")
public class SignController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // -------------------------------------------------
    //                회원가입 (Sign-Up)
    // -------------------------------------------------

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입 (Sign-Up)",
            description = "사용자 정보를 받아 회원가입 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 입력 - Validation 오류 발생",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 - 잘못된 이메일 또는 비밀번호",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UnauthorizedErrorResponse.class)
                            )
                    )
            }
    )
    public void signUpJson(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletResponse response
    ) {
        String jwtToken = authService.signUp(signUpRequest);
        Cookie jwtCookie = jwtUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);
    }

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void signUpForm(
            @Valid @ModelAttribute SignUpRequest signUpRequest,
            HttpServletResponse response
    ) {
        String jwtToken = authService.signUp(signUpRequest);
        Cookie jwtCookie = jwtUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);
    }

    // -------------------------------------------------
    //                사인인 (Sign-In)
    // -------------------------------------------------

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "사인인 (Sign-In)",
            description = "사용자 인증 정보를 받아 사인인 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사인인 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - Validation 오류 발생",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(

                            responseCode = "401",
                            description = "인증 실패 - 잘못된 이메일 또는 비밀번호",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UnauthorizedErrorResponse.class)
                            )
                    )
            }
    )


    public void signInJson(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse response
    ) {
        String jwtToken = authService.signIn(signInRequest);
        Cookie jwtCookie = jwtUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void signInForm(
            @Valid @ModelAttribute SignInRequest signInRequest,
            HttpServletResponse response
    ) {
        String jwtToken = authService.signIn(signInRequest);
        Cookie jwtCookie = jwtUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);    }

    // -------------------------------------------------
    //                사인아웃 (Sign-Out)
    // -------------------------------------------------

    @DeleteMapping("/sign-out")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "사인아웃 (Sign-Out)",
            description = "JWT 토큰 쿠키를 제거하여 사인아웃 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사인아웃 성공")
            }
    )
    public void signOut(HttpServletResponse response) {
        Cookie jwtCookie = jwtUtil.removeJwtCookie();
        response.addCookie(jwtCookie);
    }
}