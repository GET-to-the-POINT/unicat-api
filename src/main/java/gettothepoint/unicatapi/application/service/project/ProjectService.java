package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.domain.dto.project.project.ProjectRequest;
import gettothepoint.unicatapi.domain.dto.project.project.ProjectRequestWithoutFile;
import gettothepoint.unicatapi.domain.dto.project.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.project.ProjectResponseFactory;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.project.Project;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.filestorage.application.port.in.FileUploadUseCase;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final FileUploadUseCase fileUploadUseCase;
    private final ProjectResponseFactory projectResponseFactory;

    public Page<ProjectResponse> getAll(Pageable pageable) {
        Page<Project> projectPage = projectRepository.findAll(pageable);
        return projectPage.map(projectResponseFactory::fromEntity);
    }

    // 싱글
    public ProjectResponse get(Long projectId) {
        Project project = this.getOrElseThrow(projectId);
        return projectResponseFactory.fromEntity(project);
    }

    public ProjectResponse create(Long memberId) {
        ProjectRequestWithoutFile emptyRequest = ProjectRequestWithoutFile.basic();
        return create(memberId, emptyRequest);
    }

    // 생성
    public ProjectResponse create(Long memberId, ProjectRequestWithoutFile request) {
        ProjectRequest projectRequest = ProjectRequest.fromProjectRequestWithoutFile(request);
        return create(memberId, projectRequest);
    }

    public ProjectResponse create(Long memberId, ProjectRequest request) {
        Member member = memberService.getOrElseThrow(memberId);

        String titleImageKey = null;
        if (request.titleImage() != null) {
            titleImageKey = fileUploadUseCase.uploadFile(request.titleImage());
        }

        Project project = Project.builder()
                .member(member)
                .scriptTone(request.scriptTone())
                .imageStyle(request.imageStyle())
                .description(request.description())
                .title(request.title())
                .subtitle(request.subtitle())
                .templateKey(request.templateKey())
                .titleImageKey(titleImageKey)
                .build();

        this.update(project);
        return projectResponseFactory.fromEntity(project);
//        return ProjectResponse.fromEntity(project);
    }

    public void verifyProjectOwner(Long memberId, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        if (!project.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }

    public void update(Long projectId, ProjectRequestWithoutFile request) {
        ProjectRequest projectRequest = ProjectRequest.fromProjectRequestWithoutFile(request);
        update(projectId, projectRequest);
    }

    public void update(Long projectId, ProjectRequest request) {
        Project project = getOrElseThrow(projectId);
        boolean changed = false;

        if (request.scriptTone() != null) {
            project.setScriptTone(request.scriptTone());
            changed = true;
        }
        if (request.imageStyle() != null) {
            project.setImageStyle(request.imageStyle());
            changed = true;
        }
        if (request.templateKey() != null) {
            project.setTemplateKey(request.templateKey());
            changed = true;
        }
        if (request.titleImage() != null) {
            String titleUrl = fileUploadUseCase.uploadFile(request.titleImage());
            project.setTitleImageKey(titleUrl);
            changed = true;
        }

        if (StringUtils.hasText(request.description())) project.setDescription(request.description());
        if (StringUtils.hasText(request.title())) project.setTitle(request.title());
        if (StringUtils.hasText(request.subtitle())) project.setSubtitle(request.subtitle());

        if (changed) {
            project.setArtifactKey(null);
        }

        update(project);
    }

    public Project update(Project project) {
        return projectRepository.save(project);
    }

    public Project getOrElseThrow(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    }

}
