package gettothepoint.unicatapi.presentation.controller.project;

import gettothepoint.unicatapi.infrastructure.progress.ProgressManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressManager progressManager;

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

    @GetMapping("/{projectId}/progress")
    public SseEmitter subscribe(@PathVariable Long projectId) {
        System.out.println(" [SSE 연결 생성] projectId = " + projectId);
        return progressManager.createEmitter(projectId);
    }
}