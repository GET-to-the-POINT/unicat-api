package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.dto.member.MemberResponse;
import gettothepoint.unicatapi.domain.dto.member.MemberUpdateDto;
import gettothepoint.unicatapi.domain.entity.member.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "멤버 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @Operation(
        summary = "멤버 정보 조회",
        description = "로그인한 사용자의 정보를 조회하는 API입니다. JWT의 subject 값을 이용하여 memberId를 확인합니다."
    )
    public MemberResponse getMember(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        Member member = memberService.getOrElseThrow(memberId);
        return MemberResponse.fromEntity(member);
    }

    @PatchMapping
    @Operation(
        summary = "멤버 정보 수정",
        description = "로그인한 사용자의 정보를 수정합니다. 요청 바디에 수정할 내용을 포함시켜 업데이트합니다."
    )
    public void updateMember(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberUpdateDto memberUpdateDto) {
        Long memberId = Long.valueOf(jwt.getSubject());
        memberService.updateMember(memberId, memberUpdateDto);
    }
}
