package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.domain.entity.project.Section;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFilterService {

    private final StorageService storageService;

    public List<File> downloadTransitionSoundsFromSections(List<Section> sections) {
        List<File> transitionSoundFiles = new ArrayList<>();

        for (int i = 1; i < sections.size(); i++) {
            Section section = sections.get(i);
            String url = section.getTransitionUrl();

            if (StringUtils.hasText(url)) {
                try {
                    File soundFile = storageService.download(url);
                    transitionSoundFiles.add(soundFile);
                } catch (Exception e) {
                    log.warn("⚠️ 트랜지션 사운드 다운로드 실패 (sectionId: {}, url: {}): {}", section.getId(), url, e.getMessage());
                    transitionSoundFiles.add(null);
                }
            } else {
                log.info("ℹ️ sectionId {} 는 트랜지션 사운드가 없음, null 추가", section.getId());
                transitionSoundFiles.add(null);
            }
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

    public File extractThumbnail(File file) {
        if (!MediaValidationUtil.hasValidImageExtension(file.getName())) {
            throw new IllegalArgumentException("썸네일은 이미지 파일만 지원됩니다.");
        }
        return file;
    }

    public String generateRandomZoompanFilter(int totalFrames, int frameRate) {
        boolean zoomIn = new Random().nextBoolean();

        String zoomExpr = zoomIn
                ? "zoom+0.001"
                : "if(lte(zoom,1.0),1.5,max(1.001,zoom-0.0015))";

        // 랜덤 위치 선택
        String[][] positions = {
                {"iw/2-(iw/zoom/2)", "ih/2-(ih/zoom/2)"}, // center
                {"0", "0"},                              // top-left
                {"iw-(iw/zoom)", "0"},                   // top-right
                {"0", "ih-(ih/zoom)"},                   // bottom-left
                {"iw-(iw/zoom)", "ih-(ih/zoom)"}         // bottom-right
        };

        String[] selected = positions[new Random().nextInt(positions.length)];
        String x = selected[0];
        String y = selected[1];

        return "[1:v]scale=4000:-1," +
                "zoompan=z='" + zoomExpr + "':x='" + x + "':y='" + y + "':" +
                "d=" + totalFrames + ":s=1080x1080:fps=" + frameRate + "[content]";
    }

    public String generateSimpleZoompanFilter(int totalFrames, int frameRate) {
        boolean zoomIn = new Random().nextBoolean();
        String zoomExpr = zoomIn
                ? "zoom+0.001"
                : "if(lte(zoom,1.5),zoom-0.001,zoom)";

        return "[1:v]scale=4000:-1," +
                "zoompan=z='" + zoomExpr + "':" +
                "x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':" +
                "d=" + totalFrames + ":s=1080x1080:fps=" + frameRate + "[content]";
    }
}
