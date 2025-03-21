package gettothepoint.unicatapi.application.service.media;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@DisplayName("MediaServiceImpl Integration Test")
class MediaServiceImplIntegrationTest {

    // 실제 FFmpeg 실행 파일 경로 (환경에 맞게 수정)
    private static final String VALID_FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg";

    // 실제 샘플 파일들이 위치한 경로 (src/test/resources/samples/...)
    private static final Path SAMPLE_IMAGE_PATH = Paths.get("src", "test", "resources", "samples", "image", "coke.jpg");
    private static final Path SAMPLE_TITLE_PATH = Paths.get("src", "test", "resources", "samples", "image", "title.png");
    private static final Path SAMPLE_AUDIO_PATH = Paths.get("src", "test", "resources", "samples", "audio", "audio.mp3");
    private static final Path SAMPLE_BG_VIDEO_PATH = Paths.get("src", "test", "resources", "samples", "video", "Back3.mp4");
    private static final Path SAMPLE_VIDEO1_PATH = Paths.get("src", "test", "resources", "samples", "video", "test2.mp4");
    private static final Path SAMPLE_VIDEO2_PATH = Paths.get("src", "test", "resources", "samples", "video", "Test.mp4");

    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() throws Exception {
        mediaService = new MediaServiceImpl();

        // FFmpeg 경로 필드는 private이므로 reflection을 사용하여 주입
        Field ffmpegField = MediaServiceImpl.class.getDeclaredField("ffmpegPath");
        ffmpegField.setAccessible(true);
        ffmpegField.set(mediaService, VALID_FFMPEG_PATH);
    }

    @Test
    @DisplayName("단일 이미지와 오디오를 합성하여 mp4 생성")
    void testMergeImageAndAudio() {
        File imageFile = SAMPLE_IMAGE_PATH.toFile();
        File audioFile = SAMPLE_AUDIO_PATH.toFile();

        assertTrue(imageFile.exists(), "샘플 이미지 파일이 존재해야 합니다.");
        assertTrue(audioFile.exists(), "샘플 오디오 파일이 존재해야 합니다.");

        File outputFile = mediaService.mergeImageAndAudio(imageFile, audioFile);
        assertNotNull(outputFile, "생성된 출력 파일은 null이 아니어야 합니다.");
        assertTrue(outputFile.exists(), "출력 파일이 실제로 생성되어야 합니다.");
        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");

        // 테스트 후 생성된 파일 삭제 (원할 경우)
        outputFile.delete();
    }

    @Test
    @DisplayName("배경영상, 컨텐츠 이미지, 타이틀 이미지, 오디오를 합성하여 mp4 생성")
    void testMergeImageAndAudioWithBackground() {
        File bgVideo = SAMPLE_BG_VIDEO_PATH.toFile();
        File contentImage = SAMPLE_IMAGE_PATH.toFile();
        File titleImage = SAMPLE_TITLE_PATH.toFile();
        File audioFile = SAMPLE_AUDIO_PATH.toFile();

        assertTrue(bgVideo.exists(), "배경 영상 파일이 존재해야 합니다.");
        assertTrue(contentImage.exists(), "컨텐츠 이미지 파일이 존재해야 합니다.");
        assertTrue(titleImage.exists(), "타이틀 이미지 파일이 존재해야 합니다.");
        assertTrue(audioFile.exists(), "오디오 파일이 존재해야 합니다.");

        File outputFile = mediaService.mergeImageAndAudio(bgVideo, contentImage, titleImage, audioFile);
        assertNotNull(outputFile, "생성된 출력 파일은 null이 아니어야 합니다.");
        assertTrue(outputFile.exists(), "출력 파일이 실제로 생성되어야 합니다.");
        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");

        // 테스트 후 생성된 파일 삭제 (원할 경우)
        outputFile.delete();
    }

    @Test
    @DisplayName("여러 영상 파일을 병합하여 mp4 생성 (VFR)")
    void testMergeVideosAndExtractVFR() {
        File video1 = SAMPLE_VIDEO1_PATH.toFile();
        File video2 = SAMPLE_VIDEO2_PATH.toFile();

        assertTrue(video1.exists(), "첫 번째 영상 파일이 존재해야 합니다.");
        assertTrue(video2.exists(), "두 번째 영상 파일이 존재해야 합니다.");

        List<File> videoFiles = List.of(video1, video2);
        File outputFile = mediaService.mergeVideosAndExtractVFR(videoFiles);
        assertNotNull(outputFile, "병합된 출력 파일은 null이 아니어야 합니다.");
        assertTrue(outputFile.exists(), "병합된 출력 파일이 실제로 생성되어야 합니다.");
        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");

        // 테스트 후 생성된 파일 삭제 (원할 경우)
        outputFile.delete();
    }

    @Test
    @DisplayName("지원되지 않는 이미지 형식으로 mergeImageAndAudio 호출 시 예외 발생")
    void testMergeImageAndAudio_UnsupportedImageFormat(@TempDir Path tempDir) throws IOException {
        // 임시로 지원되지 않는 확장자 파일 생성
        File invalidImage = tempDir.resolve("invalid_image.txt").toFile();
        Files.write(invalidImage.toPath(), "invalid image content".getBytes());
        File audioFile = SAMPLE_AUDIO_PATH.toFile();

        Exception exception = assertThrows(ResponseStatusException.class, () ->
                mediaService.mergeImageAndAudio(invalidImage, audioFile)
        );
        assertTrue(exception.getMessage().contains("지원되지 않는 이미지 파일 형식"),
                "예외 메시지에 이미지 형식 지원 여부가 포함되어야 합니다.");
    }

    @Test
    @DisplayName("지원되지 않는 비디오 형식으로 mergeVideosAndExtractVFR 호출 시 예외 발생")
    void testMergeVideosAndExtractVFR_UnsupportedVideoFormat(@TempDir Path tempDir) throws IOException {
        // 임시로 지원되지 않는 확장자 파일 생성
        File invalidVideo = tempDir.resolve("invalid_video.xyz").toFile();
        Files.write(invalidVideo.toPath(), "dummy video content".getBytes());

        Exception exception = assertThrows(ResponseStatusException.class, () ->
                mediaService.mergeVideosAndExtractVFR(List.of(invalidVideo))
        );
        assertTrue(exception.getMessage().contains("지원되지 않는 비디오 파일 형식"),
                "예외 메시지에 비디오 형식 지원 여부가 포함되어야 합니다.");
    }
}