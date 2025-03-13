package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.SectionRequest;
import gettothepoint.unicatapi.domain.dto.project.SectionResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final SectionService sectionService;
    private final SectionRepository sectionRepository;

    public ProjectResponse getProjects(int page, int size, String sort) {
        PageRequest pageable = PageRequest.of(page, size, parseSort(sort));
        Page<Project> projectPage = projectRepository.findAll(pageable);

        if (projectPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No projects found");
        }

        return ProjectResponse.fromPage(projectPage);
    }

    private Sort parseSort(String sort) {
        String[] sortParams = sort.split(",");
        if (sortParams.length == 2) {
            return Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        }
        return Sort.by(Sort.Direction.DESC, sortParams[0]);
    }

    public Long createProject(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        Project project = Project.builder()
                .member(member)
                .build();
        projectRepository.save(project);

        return project.getId();
    }

    public List<SectionResponse> getAllSections(Long projectId) {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
        return sections.stream()
                .map(SectionResponse::fromEntity)
                .toList();
    }

    public void createVideo(List<SectionRequest> sectionRequests) {
        sectionService.createTextToSpeech(sectionRequests);
    }

}
