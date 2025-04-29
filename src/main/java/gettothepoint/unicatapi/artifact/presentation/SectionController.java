package gettothepoint.unicatapi.artifact.presentation;

import gettothepoint.unicatapi.ai.application.OpenAiService;
import gettothepoint.unicatapi.artifact.application.SectionService;
import gettothepoint.unicatapi.ai.domain.dto.CreateResourceResponse;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import gettothepoint.unicatapi.artifact.domain.dto.SectionResourceRequest;
import gettothepoint.unicatapi.artifact.domain.dto.SectionResourceRequestWithoutFile;
import gettothepoint.unicatapi.artifact.domain.dto.SectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Section", description = "섹션 API")
@RestController
@RequestMapping("/projects/{projectId}/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final OpenAiService openAiService;

    @Operation(
            summary = "프로젝트 내 섹션 목록 조회",
            description = "지정된 프로젝트의 전체 섹션을 페이지네이션과 정렬 옵션과 함께 반환합니다. (예: 각 페이지에 10개씩 표시)"
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @GetMapping
    public Page<SectionResponse> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long projectId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return sectionService.getAll(projectId, pageable);
    }

    @Operation(
            summary = "섹션 생성",
            description = "multipart/form-data 형식으로 파일과 함께 섹션 리소스를 생성합니다. 필요한 필드와 파일을 함께 전송하세요."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @ModelAttribute SectionResourceRequest request) {
        return sectionService.create(projectId, request);
    }

    @Operation(
            summary = "섹션 생성",
            description = "JSON 형식의 요청 바디를 사용하여 섹션 리소스를 생성합니다. 파일 없이 텍스트 데이터만 전달합니다."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse createWithoutFile(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @RequestBody SectionResourceRequestWithoutFile request) {
        return sectionService.create(projectId, request);
    }

    @Operation(
            summary = "섹션 상세 조회",
            description = "프로젝트와 섹션 식별자를 기반으로 특정 섹션의 상세 정보를 확인합니다. 응답 데이터에 섹션의 모든 상세정보가 포함됩니다."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @GetMapping("/{sectionId}")
    public SectionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId) {
        return sectionService.get(projectId, sectionId);
    }

    @Operation(
            summary = "섹션 리소스 수정",
            description = "multipart/form-data 형식으로 섹션의 리소스(스크립트, 이미지 등)를 수정합니다. 요청 시 필요한 항목을 전달하세요."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PatchMapping(value = "/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @ModelAttribute SectionResourceRequest request) {
        sectionService.update(projectId, sectionId, request);
    }

    @Operation(
            summary = "섹션 리소스 수정",
            description = "JSON 형식의 요청 바디를 통해 섹션의 리소스를 수정합니다. 파일 없이 텍스트 정보를 업데이트 합니다."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PatchMapping(value = "/{sectionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @RequestBody SectionResourceRequestWithoutFile request) {
        sectionService.update(projectId, sectionId, request);
    }

    @Operation(
            summary = "AI를 통한 콘텐츠 생성",
            description = "OpenAI API를 사용하여 프롬프트 기반 콘텐츠(스크립트, 이미지)를 생성하고, 해당 섹션에 추가합니다. 'type' 파라미터에 따라 콘텐츠 종류를 지정할 수 있습니다."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PostMapping(value = "/{sectionId}/ai", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateResourceResponse createContent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @RequestParam(required = false) String type, @Valid @RequestBody PromptRequest scriptRequest) {
        return openAiService.createResource(projectId, sectionId, type, scriptRequest);
    }

    @Operation(
            summary = "섹션 삭제",
            description = "프로젝트와 섹션 식별자를 받아 해당 섹션을 삭제합니다. 삭제 후 관련 리소스도 업데이트 됩니다."
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @DeleteMapping("/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId) {
        sectionService.delete(projectId, sectionId);
    }

    @Operation(
            summary = "섹션 정렬 순서 변경",
            description = "요청 바디로 전달된 새로운 순서를 기준으로 섹션의 정렬 순서를 업데이트 합니다. (숫자로 순서를 지정)"
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PostMapping("/{sectionId}/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSectionOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable String projectId, @PathVariable Long sectionId, @RequestBody int newOrder) {
        sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

}
