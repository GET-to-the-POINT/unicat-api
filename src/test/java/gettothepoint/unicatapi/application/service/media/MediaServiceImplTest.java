package gettothepoint.unicatapi.application.service.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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


        @Test
        @DisplayName("오디오 이미지 병합")
        void testMergeImageAndSoundFromFile() {
            File imageFile = new File(imagePath);
            File audioFile = new File(audioPath);

            assertTrue(imageFile.exists(), "테스트용 이미지 파일이 존재해야 합니다.");
            assertTrue(audioFile.exists(), "테스트용 오디오 파일이 존재해야 합니다.");

            File outputFile = mediaServiceImpl.mergeImageAndSoundFromFile(imageFile, audioFile);

            assertNotNull(outputFile, "출력 파일 경로가 null이면 안 됩니다.");
            assertTrue(outputFile.exists(), "출력 파일이 생성되어야 합니다.");
            assertTrue(outputFile.length() > 0, "출력 파일 크기가 0보다 커야 합니다.");

            System.out.println("🎬 생성된 영상 파일 경로: " + outputFile.getAbsolutePath());

            outputFile.delete();
        }
        @Test
        @DisplayName("이미지 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_ImageNotFound(@TempDir Path tempDir) throws IOException {

            File audioFile = new File(tempDir.toFile(), "audio.mp3");
            Files.write(audioFile.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/audio/audio.mp3")));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndSoundFromFile(new File("non_existent.jpg"), audioFile)
            );
            System.out.println("예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Image file does not exist"));
        }

        @Test
        @DisplayName("오디오 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_AudioNotFound(@TempDir Path tempDir) throws IOException {

            File imageFile = new File(tempDir.toFile(), "test.jpg");
            Files.write(imageFile.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/image/image.jpeg")));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndSoundFromFile(imageFile, new File("non_existent.mp3"))
            );

            assertTrue(exception.getMessage().contains("Audio file does not exist"));
        }

        @Test
        @DisplayName("FFmpeg 실행 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_FfmpegNotFound(@TempDir Path tempDir) throws IOException {
            // ✅ FFmpeg 경로를 비워서 실행 오류 발생 유도
            System.clearProperty("FFMPEG_PATH");

            File imageFile = new File(tempDir.toFile(), "test.jpg");
            File audioFile = new File(tempDir.toFile(), "test.mp3");

            Files.write(imageFile.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/image/image.jpeg")));
            Files.write(audioFile.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/audio/audio.mp3")));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndSoundFromFile(imageFile, audioFile)
            );

            assertTrue(exception.getMessage().contains("FFMPEG_PATH 환경 변수가 설정되지 않았습니다"));

            System.setProperty("FFMPEG_PATH", "/usr/bin/ffmpeg");
        }

        @Test
        @DisplayName("🎬 여러 비디오 파일을 병합(VFR) - 성공")
        void testMergeVideosAndExtractVFRFromFiles_Success(@TempDir Path tempDir) throws IOException {
            System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

            File video1 = new File(tempDir.toFile(), "video1.mp4");
            File video2 = new File(tempDir.toFile(), "video2.mp4");

            Files.write(video1.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/video/video.mp4")));
            Files.write(video2.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/video/video.mp4")));

            File outputVideo = mediaServiceImpl.mergeVideosAndExtractVFRFromFiles(List.of(video1, video2));

            assertNotNull(outputVideo, "Output file should not be null");
            assertTrue(outputVideo.exists(), "Output file should be created");

            System.out.println("✅ 병합된 비디오 파일 경로: " + outputVideo.getAbsolutePath());

            outputVideo.delete();
        }

        @Test
        @DisplayName("FFmpeg 경로 미설정 시 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_FfmpegNotFound(@TempDir Path tempDir) throws IOException {
            System.clearProperty("FFMPEG_PATH");

            File video1 = new File(tempDir.toFile(), "video1.mp4");
            File video2 = new File(tempDir.toFile(), "video2.mp4");

            Files.write(video1.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/video/video.mp4")));
            Files.write(video2.toPath(), Files.readAllBytes(Path.of("src/test/resources/samples/video/video.mp4")));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFRFromFiles(List.of(video1, video2))
            );

            System.out.println("❗FFmpeg 환경 변수 오류 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("FFMPEG_PATH 환경 변수가 설정되지 않았습니다"));
        }

        @Test
        @DisplayName("비디오 파일이 없을 때 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_VideoNotFound() {
            System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFRFromFiles(List.of(new File("non_existent.mp4")))
            );

            System.out.println("❗ 비디오 파일 없음 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Video file does not exist"));
        }

        @Test
        @DisplayName("지원되지 않는 형식의 비디오 파일 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_UnsupportedFormat(@TempDir Path tempDir) throws IOException {
            System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

            File invalidVideo = new File(tempDir.toFile(), "video.xyz"); // ❌ 지원되지 않는 확장자

            Files.write(invalidVideo.toPath(), new byte[1024]); // 임시 더미 파일 생성

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFRFromFiles(List.of(invalidVideo))
            );

            System.out.println("❗ 지원되지 않는 형식의 비디오 파일 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("지원되지 않는 비디오 파일 형식입니다"));
        }
    }
}

