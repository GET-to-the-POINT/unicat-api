package gettothepoint.unicatapi.application.service.video;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UploadProgressService {

    private final ConcurrentHashMap<Long, Double> progressMap = new ConcurrentHashMap<>();

    public void updateProgress(Long projectId, double progress) {
        progressMap.put(projectId, progress);
    }

    public double getProgress(Long projectId) {
        return progressMap.getOrDefault(projectId, 0.0);
    }

    public void markCompleted(Long projectId) {
        progressMap.put(projectId, 100.0);
    }

    public void markFailed(Long projectId) {
        progressMap.put(projectId, -1.0);
    }

}
