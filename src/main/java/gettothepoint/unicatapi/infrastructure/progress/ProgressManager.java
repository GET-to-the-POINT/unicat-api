package gettothepoint.unicatapi.infrastructure.progress;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProgressManager {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long projectId) {
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        emitters.put(projectId, emitter);

        emitter.onCompletion(() -> emitters.remove(projectId));
        emitter.onTimeout(() -> emitters.remove(projectId));
        emitter.onError((e) -> emitters.remove(projectId));

        return emitter;
    }

    public void send(Long projectId, int progress) {
        SseEmitter emitter = emitters.get(projectId);
        if (emitter != null) {
            try {
                System.out.println("[SSE 전송] projectId = " + projectId + ", progress = " + progress);
                emitter.send(SseEmitter.event().name("progress").data(progress));
            } catch (IOException e) {
                System.out.println("[SSE 전송 실패] projectId = " + projectId);
                emitters.remove(projectId);
            }
        } else {
            System.out.println("[SSE Emitter 없음] projectId = " + projectId);
        }
    }

    public void send(Long projectId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(projectId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitters.remove(projectId);
            }
        }
    }

    public void complete(Long projectId) {
        SseEmitter emitter = emitters.remove(projectId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    public void error(Long projectId, String message) {
        SseEmitter emitter = emitters.get(projectId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(message));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}