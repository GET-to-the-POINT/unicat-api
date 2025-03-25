package gettothepoint.unicatapi.presentation.controller.member;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.dto.member.MemberResponse;
import gettothepoint.unicatapi.domain.dto.member.MemberUpdateDto;
import gettothepoint.unicatapi.domain.entity.member.Member;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = " - Member", description = "멤버 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public MemberResponse getMember(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        Member member = memberService.getOrElseThrow(memberId);
        return MemberResponse.fromEntity(member);
    }

    @PatchMapping
    public void updateMember(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberUpdateDto memberUpdateDto) {
        Long memberId = Long.valueOf(jwt.getSubject());
        memberService.updateMember(memberId, memberUpdateDto);
    }
}
