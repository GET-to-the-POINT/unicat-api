package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.application.service.storage.AssetService;
import gettothepoint.unicatapi.domain.dto.project.ProjectRequest;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final AssetService assetService;

    public Page<ProjectResponse> getAll(Pageable pageable) {
        Page<Project> projectPage = projectRepository.findAll(pageable);
        return projectPage.map(ProjectResponse::fromEntity);
    }

    // 싱글
    public ProjectResponse get(Long projectId) {
        Project project = this.getOrElseThrow(projectId);
        return ProjectResponse.fromEntity(project);
    }

    public ProjectResponse create(Long memberId) {
        ProjectRequest emptyRequest = ProjectRequest.basic();
        return create(memberId, emptyRequest);
    }

    // 생성
    public ProjectResponse create(Long memberId, ProjectRequest request) {
        Member member = memberService.getOrElseThrow(memberId);

        Project project = Project.builder()
                .member(member)
                .templateUrl(assetService.get("template", request.templateName()))
                .scriptTone(request.scriptTone())
                .imageStyle(request.imageStyle())
                .description(request.description())
                .title(request.title())
                .subtitle(request.subtitle())
                .titleUrl(null)
                .build();

        projectRepository.save(project);
        return ProjectResponse.fromEntity(project);
    }

    public void verifyProjectOwner(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        if (!project.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }

    public Project update(Project project) {
        return projectRepository.save(project);
    }


    public ProjectResponse update(Long projectId, ProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트가 존재하지 않습니다."));

        if (request.scriptTone() != null) project.setScriptTone(request.scriptTone());
        if (request.imageStyle() != null) project.setImageStyle(request.imageStyle());
        if (request.templateName() != null) project.setTemplateUrl(request.templateName());
        if (request.description() != null) project.setDescription(request.description());
        if (request.title() != null) project.setTitle(request.title());
        if (request.subtitle() != null) project.setSubtitle(request.subtitle());

        Project updated = projectRepository.save(project);
        return ProjectResponse.fromEntity(updated);
    }

    public Project getOrElseThrow(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    }

}
