package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.dto.member.MemberResponse;
import gettothepoint.unicatapi.domain.dto.member.MemberUpdateDto;
import gettothepoint.unicatapi.domain.entity.member.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Member", description = "멤버 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "멤버 정보 조회",
            description = "사인인 한 사용자의 정보를 조회하는 API입니다. JWT의 subject 값을 이용하여 memberId를 확인합니다."
    )
    @GetMapping
    public MemberResponse getMember(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        Member member = memberService.getOrElseThrow(memberId);
        return MemberResponse.fromEntity(member);
    }

    @Operation(
            summary = "멤버 정보 수정",
            description = "사인인 한 사용자의 정보를 수정합니다. JWT에서 memberId를 추출하여 해당 사용자의 정보를 업데이트합니다."
    )
    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMember(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberUpdateDto memberUpdateDto) {
        Long memberId = Long.valueOf(jwt.getSubject());
        memberService.updateMember(memberId, memberUpdateDto);
    }
}
