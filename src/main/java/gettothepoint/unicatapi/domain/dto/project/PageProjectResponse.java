package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageProjectResponse(List<ProjectResponse> content, long totalElements, int totalPages, int currentPage,
                                  int size) {

    public static PageProjectResponse fromPage(Page<Project> projectPage) {
        List<ProjectResponse> content = projectPage.getContent().stream().map(ProjectResponse::fromEntity).toList();
        return new PageProjectResponse(content, projectPage.getTotalElements(), projectPage.getTotalPages(), projectPage.getNumber(), projectPage.getSize());
    }
}
