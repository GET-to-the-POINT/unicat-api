package gettothepoint.unicatapi.application.service.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.*;

class MediaServiceImplTest {

    private final MediaServiceImpl mediaServiceImpl = new MediaServiceImpl();

    private static final String VALID_FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg";

    private final String videoPath = Paths.get("src", "test", "resources", "samples", "video").toString();
    private final String audioPath = Paths.get("src", "test", "resources", "samples", "audio", "audio.mp3").toString();
    private final String imagePath = Paths.get("src", "test", "resources", "samples", "image", "image.jpeg").toString();

//    @BeforeEach
//    void setUp() {
//        System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);
//    }


    @BeforeEach
    void setUp() throws Exception {
        Field ffmpegField = MediaServiceImpl.class.getDeclaredField("ffmpegPath");
        ffmpegField.setAccessible(true);
        ffmpegField.set(mediaServiceImpl, VALID_FFMPEG_PATH);
    }

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
        @DisplayName("FFmpeg 실행 파일이 없을 때 예외 발생")
        void testMergeImageAndSoundFromFile_FfmpegNotFound(@TempDir Path tempDir) throws IOException {
            // ✅ FFmpeg 경로를 비워서 실행 오류 발생 유도
            System.clearProperty("FFMPEG_PATH");

            File imageFile = new File(tempDir.toFile(), "test.jpg");
            File audioFile = new File(tempDir.toFile(), "test.mp3");

            Files.write(imageFile.toPath(), Files.readAllBytes(Path.of(imagePath)));
            Files.write(audioFile.toPath(), Files.readAllBytes(Path.of(audioPath)));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeImageAndAudio(imageFile, audioFile)
            );

            assertTrue(exception.getMessage().contains("FFMPEG_PATH 환경 변수가 설정되지 않았습니다"));

            System.setProperty("FFMPEG_PATH", "/usr/bin/ffmpeg");
        }

        @Test
        @DisplayName("🎬 여러 비디오 파일을 병합(VFR) - 성공")
        void testMergeVideosAndExtractVFRFromFiles_Success(@TempDir Path tempDir) throws IOException {
            System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

            Path video1Path = Path.of(videoPath, "final_result_with_bg2.mp4");
            Path video2Path = Path.of(videoPath, "final_result_with_bg3.mp4");

            File video1 = video1Path.toFile();
            File video2 = video2Path.toFile();

            File outputVideo = mediaServiceImpl.mergeVideosAndExtractVFR(List.of(video1, video2));

            assertNotNull(outputVideo, "Output file should not be null");
            assertTrue(outputVideo.exists(), "Output file should be created");

            System.out.println("✅ 병합된 비디오 파일 경로: " + outputVideo.getAbsolutePath());

            //outputVideo.delete();
        }

        @Test
        @DisplayName("FFmpeg 경로 미설정 시 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_FfmpegNotFound(@TempDir Path tempDir) throws IOException {
            System.clearProperty("FFMPEG_PATH");

            File video1 = new File(tempDir.toFile(), "video1.mp4");
            File video2 = new File(tempDir.toFile(), "video2.mp4");

            Files.write(video1.toPath(), Files.readAllBytes(Path.of(videoPath)));
            Files.write(video2.toPath(), Files.readAllBytes(Path.of(videoPath)));

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFR(List.of(video1, video2))
            );

            System.out.println("❗FFmpeg 환경 변수 오류 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("FFMPEG_PATH 환경 변수가 설정되지 않았습니다"));
        }

        @Test
        @DisplayName("비디오 파일이 없을 때 예외 발생")
        void testMergeVideosAndExtractVFRFromFiles_VideoNotFound() {
            System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

            Exception exception = assertThrows(ResponseStatusException.class, () ->
                    mediaServiceImpl.mergeVideosAndExtractVFR(List.of(new File("non_existent.mp4")))
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
                    mediaServiceImpl.mergeVideosAndExtractVFR(List.of(invalidVideo))
            );

            System.out.println("❗ 지원되지 않는 형식의 비디오 파일 예외 발생: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("지원되지 않는 비디오 파일 형식입니다"));
        }

//    @Test
//    @DisplayName("🧪 이미지 + 음성 + 타이틀 이미지 병합 - 성공")
//    void testMergeImageAndAudioWithTitleImage() {
//        // given
//        File bgImage = new File("src/test/resources/samples/image/water.jpg");
//        File audio = new File("src/test/resources/samples/audio/audio2.mp3");
//        File titleImage = new File("src/test/resources/samples/image/title.png");
//
//        assertTrue(bgImage.exists(), "배경 이미지가 존재해야 합니다.");
//        assertTrue(audio.exists(), "오디오 파일이 존재해야 합니다.");
//        assertTrue(titleImage.exists(), "타이틀 이미지가 존재해야 합니다.");
//
//        // when
//        File result = mediaServiceImpl.mergeImageAndAudio(bgImage, audio, titleImage);
//
//        // then
//        assertNotNull(result, "출력 파일은 null이면 안 됩니다.");
//        assertTrue(result.exists(), "출력 파일이 생성되어야 합니다.");
//
//        System.out.println("🎥 생성된 영상 경로: " + result.getAbsolutePath());
//
//        // cleanup
//        //assertTrue(result.delete(), "테스트 후 생성된 파일은 삭제해야 합니다.");
//    }


    @Test
    @DisplayName("🎞️ 배경영상 + 이미지 + 타이틀 + 오디오 병합 - 성공")
    void testMergeImageAndAudioWithBackground() {
        // given
        File bgVideo = new File("src/test/resources/samples/video/back10.mp4"); // 배경 영상
        File image = new File("src/test/resources/samples/image/coke.jpg"); // 메인 이미지
        File title = new File("src/test/resources/samples/image/title.png");  // 타이틀 이미지 (투명 배경 추천)
        File audio = new File("src/test/resources/samples/audio/audio2.mp3");  // 음성

        assertTrue(bgVideo.exists(), "배경 영상이 존재해야 합니다.");
        assertTrue(image.exists(), "메인 이미지가 존재해야 합니다.");
        assertTrue(title.exists(), "타이틀 이미지가 존재해야 합니다.");
        assertTrue(audio.exists(), "오디오 파일이 존재해야 합니다.");

        // when
        File result = mediaServiceImpl.mergeImageAndAudioWithBackground(bgVideo, image, title, audio);

        // then
        assertNotNull(result, "출력 파일은 null이면 안 됩니다.");
        assertTrue(result.exists(), "출력 파일이 생성되어야 합니다.");

        System.out.println("✅ 생성된 영상 경로: " + result.getAbsolutePath());

        // optionally: 테스트 후 삭제
        // assertTrue(result.delete(), "테스트 후 생성된 파일은 삭제되어야 합니다.");
    }

















//    @Test
//    @DisplayName("🎞️ 영상 + 배경 병합 - 성공")
//    void testMergeVideoWithBackground() {
//        File mainVideo = new File("src/test/resources/samples/video/final.mp4");
//        File backgroundVideo = new File("src/test/resources/samples/video/Back.mp4");
//        File outputFile = new File("/Users/yurim/Desktop/result3.mp4");
//
//        assertTrue(mainVideo.exists(), "메인 비디오 파일이 존재해야 합니다.");
//        assertTrue(backgroundVideo.exists(), "배경 비디오 파일이 존재해야 합니다.");
//
//        File result = mediaServiceImpl.mergeImageAndAudioWithBackground(mainVideo, backgroundVideo, outputFile);
//
//        assertNotNull(result, "출력 파일은 null이면 안 됩니다.");
//        assertTrue(result.exists(), "출력 파일이 생성되어야 합니다.");
//
//        System.out.println("✅ 생성된 병합 영상: " + result.getAbsolutePath());
//    }




    }
