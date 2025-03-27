package gettothepoint.unicatapi.application.service.project;

import gettothepoint.unicatapi.application.service.member.MemberService;
import gettothepoint.unicatapi.application.service.storage.AssetService;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final AssetService assetService;

    // 컬렉션 페이지네이션
    public Page<ProjectResponse> getAll(Pageable pageable) {
        Page<Project> projectPage = projectRepository.findAll(pageable);
        return projectPage.map(ProjectResponse::fromEntity);
    }

    // 컬렉션 전체
    public List<ProjectResponse> getAll() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(ProjectResponse::fromEntity).toList();
    }

    // 싱글
    public ProjectResponse get(Long projectId) {
        Project project = this.getOrElseThrow(projectId);
        return ProjectResponse.fromEntity(project);
    }

    // 생성
    public ProjectResponse create(Long memberId) {
        Member member = memberService.getOrElseThrow(memberId);
        String templateUrl = assetService.getDefaultTemplateUrl();

        Project project = Project.builder()
                .member(member)
                .templateUrl(templateUrl)
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

    public Project getOrElseThrow(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    }

}
