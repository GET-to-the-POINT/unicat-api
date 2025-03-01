package taeniverse.unicatApi.mvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.component.schema.ErrorResponse;
import taeniverse.unicatApi.component.schema.UnauthorizedErrorResponse;
import taeniverse.unicatApi.mvc.model.dto.sign.SignInDto;
import taeniverse.unicatApi.mvc.model.dto.sign.SignUpDto;
import taeniverse.unicatApi.mvc.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Sign API", description = "회원가입, 사인인, 사인아웃 관련 API")
public class SignController {

    private final AuthService authService;

    // -------------------------------------------------
    //                회원가입 (Sign-Up)
    // -------------------------------------------------

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입 (Sign-Up) - JSON",
            description = "JSON Body로 사용자 정보를 받아 회원가입 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignUpDto.class),
                            examples = @ExampleObject(
                                    name = "SignUp JSON Example",
                                    summary = "회원가입 JSON 요청 예시",
                                    value = "{ \"email\": \"user@example.com\", \"password\": \"1234\" }"
                            )
                    )
            ),
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
            @Valid @RequestBody SignUpDto signUpDto,
            HttpServletResponse response
    ) {
        authService.signUp(signUpDto, response);
    }

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회원가입 (Sign-Up) - Form",
            description = "Form-URL-Encoded 파라미터로 사용자 정보를 받아 회원가입 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
            }
    )
    public void signUpForm(
            // @ModelAttribute를 사용하면 폼 파라미터가 DTO에 자동 바인딩됩니다.
            @ModelAttribute SignUpDto signUpDto,
            HttpServletResponse response
    ) {
        authService.signUp(signUpDto, response);
    }

    // -------------------------------------------------
    //                로그인 (Sign-In)
    // -------------------------------------------------

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "로그인 (Sign-In) - JSON",
            description = "JSON Body로 사용자 인증 정보를 받아 로그인 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignInDto.class),
                            examples = @ExampleObject(
                                    name = "SignIn JSON Example",
                                    summary = "로그인 JSON 요청 예시",
                                    value = "{ \"email\": \"user@example.com\", \"password\": \"1234\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
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
            @Valid @RequestBody SignInDto signInDto,
            HttpServletResponse response
    ) {
        authService.signIn(signInDto, response);
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "로그인 (Sign-In) - Form",
            description = "Form-URL-Encoded 파라미터로 사용자 인증 정보를 받아 로그인 처리하며, 성공 시 JWT 토큰을 쿠키에 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
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
    public void signInForm(
            @ModelAttribute SignInDto signInDto,
            HttpServletResponse response
    ) {
        authService.signIn(signInDto, response);
    }

    // -------------------------------------------------
    //                로그아웃 (Sign-Out)
    // -------------------------------------------------

    @DeleteMapping("/sign-out")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "로그아웃 (Sign-Out)",
            description = "JWT 토큰 쿠키를 제거하여 로그아웃 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
            }
    )
    public void signOut(HttpServletResponse response) {
        authService.signOut(response);
    }
}