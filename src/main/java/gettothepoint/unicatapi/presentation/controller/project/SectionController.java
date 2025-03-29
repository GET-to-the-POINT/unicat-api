package gettothepoint.unicatapi.presentation.controller.project;

import gettothepoint.unicatapi.application.service.ai.OpenAiService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.domain.dto.project.*;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;

@Tag(name = "Section", description = "섹션 API")
@RestController
@RequestMapping("/projects/{projectId}/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final OpenAiService openAiService;

    @Operation(
            summary = "섹션 목록 조회",
            description = "프로젝트 내 모든 섹션을 페이지, 정렬, 페이징 옵션과 함께 조회하는 API입니다."
    )
    @GetMapping
    public Page<SectionResponse> getAll(
            @PathVariable Long projectId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return sectionService.getAll(projectId, pageable);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse createWithoutFile(@PathVariable Long projectId, @RequestBody SectionResourceRequestWithoutFile request) {
        return sectionService.create(projectId, request);
    }

    @Operation(
            summary = "섹션 생성",
            description = "지정된 프로젝트에 새로운 섹션을 생성합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse create(@PathVariable Long projectId, SectionResourceRequest sectionResourceRequest) {
        return sectionService.create(projectId, sectionResourceRequest);
    }

    @Operation(
            summary = "섹션 상세 조회",
            description = "프로젝트 및 섹션 식별자를 사용하여 특정 섹션의 상세 정보를 반환합니다."
    )
    @GetMapping("/{sectionId}")
    public SectionResponse get(@PathVariable Long projectId, @PathVariable Long sectionId) {
        return sectionService.get(projectId, sectionId);
    }

    @Operation(
            summary = "섹션 리소스 업로드",
            description = "multipart/form-data 형식으로 섹션에 추가할 리소스를 업로드합니다."
    )
    @PostMapping(value = "/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResourceResponse uploadResource(@PathVariable Long projectId, @PathVariable Long sectionId, @ModelAttribute SectionResourceRequest sectionUploadResourceRequest) {
        return sectionService.update(projectId, sectionId, sectionUploadResourceRequest);
    }

    @Operation(
            summary = "콘텐츠 생성 요청",
            description = "OpenAI API를 사용해 프롬프트 기반 콘텐츠를 생성하여 섹션에 추가합니다. (transitionName 및 type 옵션 제공)"
    )
    @PreAuthorize("@projectService.verifyProjectOwner(#jwt.subject, #projectId)")
    @PostMapping(value = "/{sectionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateResourceResponse createContent(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @RequestParam(required = false) String type, @Valid @RequestBody PromptRequest scriptRequest) {
        return openAiService.createResource(projectId, sectionId, type, scriptRequest);
    }

    @Operation(
            summary = "섹션 정렬 순서 변경",
            description = "요청 바디로 전달된 새로운 순서를 기준으로 섹션의 정렬 순서를 업데이트합니다."
    )
    @PostMapping("/{sectionId}/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }

    @InitBinder("sectionResourceRequest")
    public void initSectionResourceBinder(WebDataBinder binder) {
        binder.setIgnoreInvalidFields(true);
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(null);
            }
        });
    }
}
