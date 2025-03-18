package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class VoiceMergeImageService {

    private final SectionRepository sectionRepository;

    public void createVideosForEachSection(Long projectId) throws IOException {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("해당 프로젝트의 섹션을 찾을 수 없습니다: " + projectId);
        }

        for (Section section : sections) {
            String outputFile = "output_section" + section.getId() + ".mp4";
            voiceMergeImage(section, outputFile);
        }
    }

    private String voiceMergeImage(Section section, String outputFile) throws IOException {
        String imagePath = section.getImageUrl();
        String audioPath = section.getTtsUrl();

        ProcessBuilder builder = new ProcessBuilder(
                "/opt/homebrew/Cellar/ffmpeg/7.1.1_1/bin/ffmpeg",
                "-loop", "1",
                "-i", imagePath,
                "-i", audioPath,
                "-vf", "scale=1080:1350:force_original_aspect_ratio=increase,crop=1080:1350,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                "-c:v", "libx264",
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-movflags", "+faststart",
                "-shortest",
                outputFile
        );

        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new VideoException("FFmpeg 실행 중 오류 발생. 종료 코드: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoException("FFmpeg 실행 중 예외 발생", e);
        }

        return new File(outputFile).getAbsolutePath();
    }

}