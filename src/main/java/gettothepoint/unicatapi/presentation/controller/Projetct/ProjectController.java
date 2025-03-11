package gettothepoint.unicatapi.presentation.controller.Projetct;

import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.presentation.controller.video.YoutubeDataController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects") // 프로젝트 조회 API
    public  ProjectResponse getProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        int adjustedPage = (page > 0) ? page - 1 : 0;
        return projectService.getProjects(adjustedPage, size, sort);
    }
}