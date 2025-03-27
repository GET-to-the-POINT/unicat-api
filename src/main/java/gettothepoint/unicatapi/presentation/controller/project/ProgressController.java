package gettothepoint.unicatapi.presentation.controller.project;

import gettothepoint.unicatapi.infrastructure.progress.ProgressManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Project", description = "업로드 진행률 관련 API")
public class ProgressController {

    private final ProgressManager progressManager;

    @Operation(
            summary = "샘플 진행률 SSE 테스트",
            description = "샘플 데이터를 사용하여 SSE 방식으로 진행률 이벤트(0~100%)를 테스트하는 API입니다."
    )
    @GetMapping("/progress")
    public SseEmitter progress() {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(i));
                    Thread.sleep(3000);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Operation(
            summary = "프로젝트 업로드 진행률 구독",
            description = "특정 프로젝트의 유튜브 업로드 진행률을 SSE 방식으로 구독합니다. projectId 경로 변수를 사용합니다."
    )
    @GetMapping("/{projectId}/progress")
    public SseEmitter subscribe(@PathVariable Long projectId) {
        System.out.println(" [SSE 연결 생성] projectId = " + projectId);
        return progressManager.createEmitter(projectId);
    }
}
