package gettothepoint.unicatapi.presentation.controller.project;

import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.domain.dto.project.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import java.security.MessageDigest;

@Tag(name = "Project Section", description = "섹션 API")
@RestController
@RequestMapping("/projects/{projectId}/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final OpenAiService openAiService;

    @GetMapping
    public Page<SectionResponse> getAll(@PathVariable Long projectId, Pageable pageable) {
        return sectionService.getAll(projectId, pageable);
    }

    @PostMapping
    public SectionResponse create(@PathVariable Long projectId) {
        return sectionService.create(projectId);
    }

    @GetMapping("/{sectionId}")
    public SectionResponse get(@PathVariable Long projectId, @PathVariable Long sectionId) {
        return sectionService.get(projectId, sectionId);
    }

    @PostMapping(value = "/{sectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResourceResponse uploadResource(@PathVariable Long projectId, @PathVariable Long sectionId, @Valid @ModelAttribute SectionResourceRequest sectionUploadResourceRequest) throws IOException, NoSuchAlgorithmException {
        return sectionService.uploadResource(projectId, sectionId, sectionUploadResourceRequest);
    }

    @PreAuthorize("@projectService.verifyProjectOwner(#projectId, jwt.getSubject())")
    @PostMapping(value = "/{sectionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CreateResourceResponse createResource(@AuthenticationPrincipal Jwt jwt, @PathVariable Long projectId, @PathVariable Long sectionId, @RequestParam(required = false) String type, @Valid @RequestBody PromptRequest scriptRequest) {
        return openAiService.createResource(projectId, sectionId, type, scriptRequest);
    }

    @PostMapping("/{sectionId}/order")
    public Long updateSectionOrder(@PathVariable Long sectionId, @RequestBody int newOrder) {
        return sectionService.updateSectionSortOrder(sectionId, newOrder);
    }


}
