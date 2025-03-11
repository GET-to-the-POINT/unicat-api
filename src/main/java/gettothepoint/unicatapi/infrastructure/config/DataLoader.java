package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.domain.entity.Project;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DataLoader {
    private final ProjectRepository projectRepository;

    @PostConstruct
    public void init() {
        if (projectRepository.count() == 0) { // 데이터가 없을 때만 실행
            IntStream.range(1, 21).forEach(i ->
                    projectRepository.save(Project.builder()
                            .title("Project Title " + i)
                            .subtitle("Brief description of project " + i)
                            .imageUrl("https://example.com/images/project" + i + ".jpg")
                            .projectUrl("https://example.com/videos/project" + i + ".mp4")
                            .build())
            );
        }
    }
}