package gettothepoint.unicatapi.application.service.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceImplTest {

    private final MediaServiceImpl mediaServiceImpl = new MediaServiceImpl();

    private static final String VALID_FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg";

    private final String videoPath = Paths.get("src", "test", "resources", "samples", "video", "video.mp4").toString();
    private final String audioPath = Paths.get("src", "test", "resources", "samples", "audio", "audio.mp3").toString();
    private final String imagePath = Paths.get("src", "test", "resources", "samples", "image", "image.jpeg").toString();

    @BeforeEach
    void setUp() {
        System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);
    }

    @Nested
    @DisplayName("비디오 병합 테스트")
    class videoMerge {

        @Test
        @DisplayName("비디오 병합 성공")
        void testMergeVideos() {
            String outputFile = mediaServiceImpl.videos(List.of(videoPath, videoPath));
            assertNotNull(outputFile, "Output file path should not be null");

            File output = new File(outputFile);
            assertTrue(output.exists(), "Output file should be created");

            output.delete();
        }

        @Test
        @DisplayName("비디오 파일 경로가 비어있는 경우 예외 발생")
        void testMergeVideos_EmptyPath() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(List.of("", videoPath)));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("비디오 파일 경로가 null인 경우 예외 발생")
        void testMergeVideos_NullPath() {
            List<String> videoPaths = new ArrayList<>();
            videoPaths.add(null);
            videoPaths.add(videoPath);
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(videoPaths));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("비디오 파일 컬렉션을 null로 전달한 경우 예외 발생")
        void testMergeVideos_NullCollection() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(null));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("비디오 파일이 존재하지 않는 경우 예외 발생")
        void testMergeVideos_FileNotFound() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(List.of("nonexistent.mp4", videoPath)));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("비디오 파일 리스트가 비어있는 경우 예외 발생")
        void testMergeVideos_EmptyList() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(List.of()));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("FFmpeg 경로가 잘못된 경우 예외 발생")
        void testMergeVideos_InvalidFFmpegPath() {
            System.setProperty("FFMPEG_PATH", "invalid/path/to/ffmpeg");
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(List.of(videoPath, videoPath)));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("FFmpeg 경로가 설정되지 않은 경우 예외 발생")
        void testMergeVideos_NullFFmpegPath() {
            System.clearProperty("FFMPEG_PATH");
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.videos(List.of(videoPath, videoPath)));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("오디오 이미지 병합 테스트")
    class audioVideoMerge {

        @Test
        @DisplayName("오디오 이미지 병합 성공")
        void audioAndVideo() {
            String outputFile = mediaServiceImpl.audioAndVideo(audioPath, imagePath);
            assertNotNull(outputFile, "Output file path should not be null");

            File output = new File(outputFile);
            assertTrue(output.exists(), "Output file should be created");

            output.delete();
        }

        @Test
        @DisplayName("오디오 파일 경로가 비어있는 경우 예외 발생")
        void testAudioAndVideo_EmptyAudioPath() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo("", imagePath));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("이미지 파일 경로가 비어있는 경우 예외 발생")
        void testAudioAndVideo_EmptyimagePath() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(audioPath, ""));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("오디오 파일 경로가 null인 경우 예외 발생")
        void testAudioAndVideo_NullAudioPath() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(null, imagePath));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("이미지 파일 경로가 null인 경우 예외 발생")
        void testAudioAndVideo_NullimagePath() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(audioPath, null));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("오디오 이미지 파일 둘 다 null인 경우 예외 발생")
        void testAudioAndVideo_NullBothPaths() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(null, null));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("ffmpeg 경로가 잘못된 경우 예외 발생")
        void testAudioAndVideo_InvalidFFmpegPath() {
            System.setProperty("FFMPEG_PATH", "invalid/path/to/ffmpeg");
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(audioPath, imagePath));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

        @Test
        @DisplayName("ffmpeg 경로가 null인 경우 예외 발생")
        void testAudioAndVideo_NullFFmpegPath() {
            System.clearProperty("FFMPEG_PATH");
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> mediaServiceImpl.audioAndVideo(audioPath, imagePath));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        }

    }

}