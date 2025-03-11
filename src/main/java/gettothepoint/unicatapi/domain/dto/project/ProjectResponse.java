package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.Project;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProjectResponse (
        List<ProjectDTO> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {
    public record ProjectDTO(String title, String subtitle, String imageUrl,String projectUrl) {
        public static ProjectDTO fromEntity(Project project) {
            return new ProjectDTO(project.getTitle(), project.getSubtitle(), project.getImageUrl(), project.getProjectUrl());
        }
    }

    public static ProjectResponse fromPage(Page<Project> projectPage) {
        List<ProjectDTO> dtoList = projectPage.getContent()
                .stream()
                .map(ProjectDTO::fromEntity)
                .toList();
        return new ProjectResponse(
                dtoList,
                projectPage.getTotalElements(),
                projectPage.getTotalPages(),
                projectPage.getNumber(),
                projectPage.getSize()
        );
    }
}
