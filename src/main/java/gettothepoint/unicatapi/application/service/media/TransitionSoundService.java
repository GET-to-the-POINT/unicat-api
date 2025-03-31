package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.storage.AssetService;
import gettothepoint.unicatapi.application.service.storage.SupabaseStorageServiceImpl;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransitionSoundService {

    private final SupabaseStorageServiceImpl supabaseStorageService;
    private final AssetService assetService;

    public List<File> downloadTransitionSoundsFromSections(List<Section> sections) {
        List<File> transitionSoundFiles = new ArrayList<>();

        for (Section section : sections) {
            String url = section.getTransitionUrl();

            File soundFile;

            if (StringUtils.hasText(url)) {
                try {
                    soundFile = supabaseStorageService.download(url);
                } catch (Exception e) {
                    log.warn("⚠️ 트랜지션 사운드 다운로드 실패 (sectionId: {}, url: {}): {}", section.getId(), url, e.getMessage());
                    soundFile = loadDefaultTransitionSound();
                }
            } else {
                log.info("ℹ️ sectionId {} 는 트랜지션 사운드가 없음, 기본 효과음으로 대체", section.getId());
                soundFile = loadDefaultTransitionSound();
            }
            transitionSoundFiles.add(soundFile);
        }
        return transitionSoundFiles;
    }

    public double getAudioDurationInSeconds(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                p.waitFor();
                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            throw new MediaServiceImpl.MediaProcessingException("음성 길이 가져오기 실패", e);
        }
    }

    public long getVideoDurationInMs(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-select_streams", "v:0",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                p.waitFor();
                return (long) (Double.parseDouble(line.trim()) * 1000);
            }
        } catch (Exception e) {
            throw new MediaServiceImpl.MediaProcessingException("비디오 길이 가져오기 실패", e);
        }
    }
    private File loadDefaultTransitionSound() {
        try {
            String defaultUrl = assetService.get("transition", "transition1.mp3"); // Supabase public URL
            return supabaseStorageService.download(defaultUrl);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "기본 트랜지션 사운드 다운로드 실패", e);
        }
    }
}
