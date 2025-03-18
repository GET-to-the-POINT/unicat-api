package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.application.service.video.VideoException;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MergeService {

    private static final String FFMPEG_PATH = System.getProperty("FFMPEG_PATH", "/opt/homebrew/bin/ffmpeg");
    private final SectionRepository sectionRepository;
    private final SupabaseStorageService storageService;
    private final ProjectRepository projectRepository;


    public String createArtifactVideo(Long projectId) throws IOException, InterruptedException {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("해당 프로젝트에 포함된 섹션이 없습니다: " + projectId);
        }

        List<String> videoPaths = sections.stream()
                .map(Section::getVideoUrl)
                .collect(Collectors.toList());

        String outputFile = "/Users/yurim/project/unicat-api/" + projectId + "_artifact.mp4";

        mergeVideos(videoPaths, outputFile);

        String supabaseUrl = uploadMergedVideoToSupabase(new File(outputFile));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        project.setArtifactUrl(supabaseUrl);
        projectRepository.save(project);

        return supabaseUrl;
    }


    private void mergeVideos(List<String> inputFiles, String outputFile) throws IOException, InterruptedException {
        if (FFMPEG_PATH == null || FFMPEG_PATH.isEmpty()) {
            throw new IllegalStateException("FFMPEG_PATH 환경 변수가 설정되지 않았습니다. FFmpeg 경로를 확인하세요.");
        }

        File outputDir = new File(outputFile).getParentFile();
        if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException(" 출력 디렉토리 생성 실패: " + outputDir.getAbsolutePath());
        }

        // 2️⃣ FFmpeg에 사용할 파일 목록 작성 (file_list.txt)
        File tempFileList = new File(outputDir, "file_list.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFileList))) {
            for (String file : inputFiles) {
                writer.write("file '" + file + "'\n");
            }
        }

        // 3️⃣ FFmpeg 명령어 실행 (concat 방식으로 병합)
        ProcessBuilder builder = new ProcessBuilder(
                FFMPEG_PATH,
                "-f", "concat",
                "-safe", "0",
                "-i", tempFileList.getAbsolutePath(),
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "experimental",
                outputFile
        );
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // 4️⃣ FFmpeg 실행 로그 출력
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }

        // 5️⃣ FFmpeg 종료 코드 확인
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new VideoException("FFmpeg 실행 중 오류 발생. 종료 코드: " + exitCode);
        }

        // 6️⃣ 임시 파일 삭제
        if (tempFileList.delete()) {
            log.info("임시 파일 삭제 완료: " + tempFileList.getAbsolutePath());
        } else {
            log.warn("임시 파일 삭제 실패: " + tempFileList.getAbsolutePath());
        }

        log.info(" 동영상 병합 성공! 저장 위치: " + outputFile);
    }


    private String uploadMergedVideoToSupabase(File mergedFile) {
        MultipartFile multipartFile = new MultipartFileUtil(mergedFile, "artifact_video", "video/mp4");
        return storageService.uploadFile(multipartFile);
    }

}