package gettothepoint.unicatapi.application.service.video;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UploadProgressService {

    private final Map<Long, Double> progressMap = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    // 업로드 진행률 업데이트
    public void updateProgress(Long projectId, double progress) {
        progressMap.put(projectId, progress);
        sendProgressUpdate(projectId, progress);
    }

    // SSE 구독자 등록
    public SseEmitter subscribeToProgress(Long projectId) {
        SseEmitter emitter = new SseEmitter(60_000L); // 60초 타임아웃
        emitterMap.put(projectId, emitter);
        sendProgressUpdate(projectId, progressMap.getOrDefault(projectId, 0.0));
        return emitter;
    }

    // SSE 클라이언트에게 진행률 전송
    private void sendProgressUpdate(Long projectId, double progress) {
        SseEmitter emitter = emitterMap.get(projectId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(progress));
                if (progress >= 100.0 || progress < 0) {
                    emitter.complete();
                    emitterMap.remove(projectId);
                }
            } catch (IOException e) {
                emitter.complete();
                emitterMap.remove(projectId);
            }
        }
    }

    // 업로드 완료 시
    public void markCompleted(Long projectId) {
        updateProgress(projectId, 100.0);
    }

    // 업로드 실패 시
    public void markFailed(Long projectId) {
        updateProgress(projectId, -1.0);
    }
}