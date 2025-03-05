package taeniverse.unicatApi.mvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Token API", description = "토큰 리프레시 관련 엔드포인트를 제공합니다.") // 컨트롤러에 태그 추가하여 API 문서에서 그룹화
public class TokenController {

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "토큰 리프레시",
            description = "쿠키에 담긴 토큰을 직접 리프레시 합니다. 모든 요청에 토큰이 리프레시 되기 때문에 별도의 요청이 필요하지 않습니다."
    )
    @ApiResponse(
            responseCode = "204",
            description = "토큰이 성공적으로 갱신되었으나, 응답 본문은 없습니다."
    )
    public void refreshToken() {
        // 필터 체이닝에서 토큰을 갱신하기 때문에 여기서는 엔드포인트만 만들어준다.
    }
}