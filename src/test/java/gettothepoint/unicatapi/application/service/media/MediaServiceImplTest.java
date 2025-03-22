package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("MediaServiceImpl Test")
class MediaServiceImplTest {

    @Autowired
    MediaServiceImpl mediaServiceImpl;

    @Autowired
    StorageService storageService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TextToSpeechService textToSpeechService() {
            return Mockito.mock(TextToSpeechService.class);
        }
    }

    // 실제 FFmpeg 실행 파일 경로 (환경에 맞게 수정)
    private static final String VALID_FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg";

    // 실제 샘플 파일들이 위치한 경로 (src/test/resources/samples/...)
    private static final String BACKGROUND_URL = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/video/uploads/Back3.mp4";
    private static final String CONTENT_IMAGE_URL = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/image/uploads/coke.jpg";
    private static final String TITLE_IMAGE_URL = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/image/uploads/title.png";
    private static final String AUDIO_URL = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/voice/uploads/audio.mp3";

    @BeforeEach
    void setupSupabaseFiles() {
        // Supabase에 있는 파일이 임시저장소에 복사되어 있는지 확인 (다운로드 수행)
        File bgFile = storageService.download(BACKGROUND_URL);
        File contentFile = storageService.download(CONTENT_IMAGE_URL);
        File titleFile = storageService.download(TITLE_IMAGE_URL);
        File audioFile = storageService.download(AUDIO_URL);

        assertTrue(bgFile.exists(), "배경 영상 파일이 임시저장소에 존재해야 합니다.");
        assertTrue(contentFile.exists(), "컨텐츠 이미지 파일이 임시저장소에 존재해야 합니다.");
        assertTrue(titleFile.exists(), "타이틀 이미지 파일이 임시저장소에 존재해야 합니다.");
        assertTrue(audioFile.exists(), "오디오 파일이 임시저장소에 존재해야 합니다.");

        System.out.println("Supabase 파일 다운로드 확인:");
        System.out.println("  배경 영상: " + bgFile.getAbsolutePath());
        System.out.println("  컨텐츠 이미지: " + contentFile.getAbsolutePath());
        System.out.println("  타이틀 이미지: " + titleFile.getAbsolutePath());
        System.out.println("  오디오: " + audioFile.getAbsolutePath());
    }

//
//
//    @Test
//    @DisplayName("단일 이미지와 오디오를 합성하여 mp4 생성")
//    void testMergeImageAndAudio() {
//        File imageFile = SAMPLE_IMAGE_PATH.toFile();
//        File audioFile = SAMPLE_AUDIO_PATH.toFile();
//
//        assertTrue(imageFile.exists(), "샘플 이미지 파일이 존재해야 합니다.");
//        assertTrue(audioFile.exists(), "샘플 오디오 파일이 존재해야 합니다.");
//
//        File outputFile = mediaServiceImpl.mergeImageAndAudio(imageFile, audioFile);
//        assertNotNull(outputFile, "생성된 출력 파일은 null이 아니어야 합니다.");
//        assertTrue(outputFile.exists(), "출력 파일이 실제로 생성되어야 합니다.");
//        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");
//
//        // 테스트 후 생성된 파일 삭제 (원할 경우)
//        outputFile.delete();
//    }
//
//    @Test
//    @DisplayName("배경영상, 컨텐츠 이미지, 타이틀 이미지, 오디오를 합성하여 mp4 생성")
//    void testMergeImageAndAudioWithBackground() {
//        File bgVideo = SAMPLE_BG_VIDEO_PATH.toFile();
//        File contentImage = SAMPLE_IMAGE_PATH.toFile();
//        File titleImage = SAMPLE_TITLE_PATH.toFile();
//        File audioFile = SAMPLE_AUDIO_PATH.toFile();
//
//        assertTrue(bgVideo.exists(), "배경 영상 파일이 존재해야 합니다.");
//        assertTrue(contentImage.exists(), "컨텐츠 이미지 파일이 존재해야 합니다.");
//        assertTrue(titleImage.exists(), "타이틀 이미지 파일이 존재해야 합니다.");
//        assertTrue(audioFile.exists(), "오디오 파일이 존재해야 합니다.");
//
//        File outputFile = mediaServiceImpl.mergeImageAndAudio(bgVideo, contentImage, titleImage, audioFile);
//        assertNotNull(outputFile, "생성된 출력 파일은 null이 아니어야 합니다.");
//        assertTrue(outputFile.exists(), "출력 파일이 실제로 생성되어야 합니다.");
//        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");
//
//        // 테스트 후 생성된 파일 삭제 (원할 경우)
//        outputFile.delete();
//    }
//
//    @Test
//    @DisplayName("여러 영상 파일을 병합하여 mp4 생성 (VFR)")
//    void testMergeVideosAndExtractVFR() {
//        File video1 = SAMPLE_VIDEO1_PATH.toFile();
//        File video2 = SAMPLE_VIDEO2_PATH.toFile();
//
//        assertTrue(video1.exists(), "첫 번째 영상 파일이 존재해야 합니다.");
//        assertTrue(video2.exists(), "두 번째 영상 파일이 존재해야 합니다.");
//
//        List<File> videoFiles = List.of(video1, video2);
//        File outputFile = mediaServiceImpl.mergeVideosAndExtractVFR(videoFiles);
//        assertNotNull(outputFile, "병합된 출력 파일은 null이 아니어야 합니다.");
//        assertTrue(outputFile.exists(), "병합된 출력 파일이 실제로 생성되어야 합니다.");
//        assertTrue(outputFile.getName().endsWith(".mp4"), "출력 파일 확장자는 .mp4여야 합니다.");
//
//        // 테스트 후 생성된 파일 삭제 (원할 경우)
//        outputFile.delete();
//    }
//
//    @Test
//    @DisplayName("지원되지 않는 이미지 형식으로 mergeImageAndAudio 호출 시 예외 발생")
//    void testMergeImageAndAudio_UnsupportedImageFormat(@TempDir Path tempDir) throws IOException {
//        // 임시로 지원되지 않는 확장자 파일 생성
//        File invalidImage = tempDir.resolve("invalid_image.txt").toFile();
//        Files.write(invalidImage.toPath(), "invalid image content".getBytes());
//        File audioFile = SAMPLE_AUDIO_PATH.toFile();
//
//        Exception exception = assertThrows(ResponseStatusException.class, () ->
//                mediaServiceImpl.mergeImageAndAudio(invalidImage, audioFile)
//        );
//        assertTrue(exception.getMessage().contains("지원되지 않는 이미지 파일 형식"),
//                "예외 메시지에 이미지 형식 지원 여부가 포함되어야 합니다.");
//    }
//
//    @Test
//    @DisplayName("지원되지 않는 비디오 형식으로 mergeVideosAndExtractVFR 호출 시 예외 발생")
//    void testMergeVideosAndExtractVFR_UnsupportedVideoFormat(@TempDir Path tempDir) throws IOException {
//        // 임시로 지원되지 않는 확장자 파일 생성
//        File invalidVideo = tempDir.resolve("invalid_video.xyz").toFile();
//        Files.write(invalidVideo.toPath(), "dummy video content".getBytes());
//
//        Exception exception = assertThrows(ResponseStatusException.class, () ->
//                mediaServiceImpl.mergeVideosAndExtractVFR(List.of(invalidVideo))
//        );
//        assertTrue(exception.getMessage().contains("지원되지 않는 비디오 파일 형식"),
//                "예외 메시지에 비디오 형식 지원 여부가 포함되어야 합니다.");
//    }
//
//
//    @Test
//    @DisplayName("🎬 Supabase 리소스로 영상 병합 - 통합 테스트")
//    void testMergeWithSupabaseResources() {
//        // given
//        String backgroundUrl = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/video/uploads/Back3.mp4";
//        String contentImageUrl = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/image/uploads/coke.jpg";
//        String titleImageUrl = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/image/uploads/title.png";
//        String audioUrl = "https://bhqvrnbzzqzqlwwrcgbm.supabase.co/storage/v1/object/public/voice/uploads/audio.mp3";
//
//
//
//        // when
//        File result = mediaServiceImpl.mergeImageAndAudio(
//                backgroundUrl,
//                contentImageUrl,
//                titleImageUrl,
//                audioUrl
//        );
//
//        // then
//        assertNotNull(result, "병합된 파일이 null이면 안 됩니다.");
//        assertTrue(result.exists(), "병합된 영상 파일이 실제로 존재해야 합니다.");
//        System.out.println("✅ 병합된 파일 경로: " + result.getAbsolutePath());
//    }

    @Test
    @DisplayName("🎬 Supabase 리소스로 영상 병합 - 통합 테스트")
    void testMergeWithSupabaseResources() {
        // given
        String backgroundUrl = BACKGROUND_URL;
        String contentImageUrl = CONTENT_IMAGE_URL;
        String titleImageUrl = TITLE_IMAGE_URL;
        String audioUrl = AUDIO_URL;

        System.out.println("🚀 테스트 시작: Supabase URL 사용");
        System.out.println("🎥 배경 URL: " + backgroundUrl);
        System.out.println("🖼️ 컨텐츠 이미지 URL: " + contentImageUrl);
        System.out.println("🎵 오디오 URL: " + audioUrl);

        // when
        File result = mediaServiceImpl.mergeImageAndAudio(
                backgroundUrl,
                contentImageUrl,
                titleImageUrl,
                audioUrl
        );

        // then
        assertNotNull(result, "병합된 파일이 null이면 안 됩니다.");
        assertTrue(result.exists(), "병합된 영상 파일이 실제로 존재해야 합니다.");
        System.out.println("✅ 병합된 파일 경로: " + result.getAbsolutePath());
    }
}