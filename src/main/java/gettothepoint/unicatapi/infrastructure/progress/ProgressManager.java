package gettothepoint.unicatapi.infrastructure.progress;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ProgressManager {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long projectId) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        emitters.put(projectId, emitter);

        emitter.onCompletion(() -> emitters.remove(projectId));
        emitter.onTimeout(() -> emitters.remove(projectId));
        emitter.onError(e -> emitters.remove(projectId));

        return emitter;
    }

    public void send(Long projectId, int progress) {
        SseEmitter emitter = emitters.get(projectId);
        if (emitter != null) {
            try {
                log.info("Sending progress {}% to project {}", progress, projectId);
                emitter.send(SseEmitter.event().name("progress").data(progress));
            } catch (IOException e) {
                log.error("Failed to send progress to project {}: {}", projectId, e.getMessage());
                emitters.remove(projectId);
            }
        } else {
            log.warn("No emitter found for project {}", projectId);
        }
    }

    public void complete(Long projectId) {
        SseEmitter emitter = emitters.remove(projectId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}