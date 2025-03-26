package gettothepoint.unicatapi.application.service.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceImplTest {

    private final MediaServiceImpl mediaServiceImpl = new MediaServiceImpl();

    private final String videoPath = Paths.get("src", "test", "resources", "samples", "video", "video.mp4").toString();
    private final String audioPath = Paths.get("src", "test", "resources", "samples", "audio", "audio.mp3").toString();
    private final String imagePath = Paths.get("src", "test", "resources", "samples", "image", "image.jpeg").toString();

    @Nested
    @DisplayName("비디오 병합 테스트")
    class videoMerge {

        @Test
        @DisplayName("오디오 이미지 병합")
        void testMergeImageAndSoundFromFile() {
            File imageFile = new File(imagePath);
            File audioFile = new File(audioPath);

            assertTrue(imageFile.exists(), "테스트용 이미지 파일이 존재해야 합니다.");
            assertTrue(audioFile.exists(), "테스트용 오디오 파일이 존재해야 합니다.");

            // Call the method which now returns an InputStream
            File outputFile = mediaServiceImpl.mergeImageAndAudio(imageFile, audioFile);
            assertNotNull(outputFile, "출력 스트림이 null이면 안 됩니다.");

            if (outputFile.exists()) {
                assertTrue(outputFile.delete(), "테스트 후 생성된 파일을 삭제해야 합니다.");
            }
        }

        @Test
        @DisplayName("이미지 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_ImageNotFound(@TempDir Path tempDir) throws IOException {

            File audioFile = new File(tempDir.toFile(), "audio.mp3");
            Files.write(audioFile.toPath(), Files.readAllBytes(Path.of(audioPath)));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndAudio(new File("non_existent.jpg"), audioFile)
            );
            System.out.println("예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Image file does not exist"));
        }

        @Test
        @DisplayName("오디오 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_AudioNotFound(@TempDir Path tempDir) throws IOException {

            File imageFile = new File(tempDir.toFile(), "test.jpg");
            Files.write(imageFile.toPath(), Files.readAllBytes(Path.of(imagePath)));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndAudio(imageFile, new File("non_existent.mp3"))
            );

            assertTrue(exception.getMessage().contains("Audio file does not exist"));
        }

        @Test
        @DisplayName("🎬 여러 비디오 파일을 병합(VFR) - 성공")
        void testMergeVideosAndExtractVFRFromFiles_Success(@TempDir Path tempDir) throws IOException {

            File video1 = new File(tempDir.toFile(), "video1.mp4");
            File video2 = new File(tempDir.toFile(), "video2.mp4");
            Path path = Path.of(videoPath);
            Files.write(video1.toPath(), Files.readAllBytes(path));
            Files.write(video2.toPath(), Files.readAllBytes(path));

            File outputVideo = mediaServiceImpl.mergeVideosAndExtractVFR(List.of(video1, video2));

            assertNotNull(outputVideo, "Output file should not be null");
            assertTrue(outputVideo.exists(), "Output file should be created");

            System.out.println("✅ 병합된 비디오 파일 경로: " + outputVideo.getAbsolutePath());

            outputVideo.delete();
        }

        @Test
        @DisplayName("비디오 파일이 없을 때 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_VideoNotFound() {

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFR(List.of(new File("non_existent.mp4")))
            );

            System.out.println("❗ 비디오 파일 없음 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Video file does not exist"));
        }

        @Test
        @DisplayName("지원되지 않는 형식의 비디오 파일 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_UnsupportedFormat(@TempDir Path tempDir) throws IOException {

            File invalidVideo = new File(tempDir.toFile(), "video.xyz"); // ❌ 지원되지 않는 확장자

            Files.write(invalidVideo.toPath(), new byte[1024]); // 임시 더미 파일 생성

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFR(List.of(invalidVideo))
            );

            System.out.println("❗ 지원되지 않는 형식의 비디오 파일 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("지원되지 않는 비디오 파일 형식입니다"));
        }
    }
}
