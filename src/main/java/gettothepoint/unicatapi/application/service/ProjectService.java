package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

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
}
