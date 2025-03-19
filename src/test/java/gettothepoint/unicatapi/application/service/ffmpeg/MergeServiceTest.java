package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MergeServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @Spy
    @InjectMocks
    private MergeService mergeService;

    private static final String VALID_FFMPEG_PATH = "/opt/homebrew/bin/ffmpeg"; // 실제 환경에 맞게 수정

    @BeforeEach
    void setUp() {
        // 테스트 시작 전, FFMPEG_PATH를 유효한 값으로 설정
        System.setProperty("FFMPEG_PATH", VALID_FFMPEG_PATH);

        // 성공 케이스에서는 실제 FFmpeg 실행을 막기 위해 부분 모킹 처리
        doNothing().when(mergeService).executeFfmpegCommand(any(ProcessBuilder.class));
    }

    @Nested
    @DisplayName("videos() 테스트")
    class VideosTest {

        @Test
        @DisplayName("정상 케이스: 파일 리스트를 입력하면 예외 없이 성공")
        void testVideos_Success() {
            // given
            List<String> inputFiles = List.of("/tmp/video1.mp4", "/tmp/video2.mp4");
            String outputFile = "/tmp/output.mp4";

            // when & then (실제 FFmpeg 호출은 doNothing() 처리되어 예외 발생하지 않음)
            assertDoesNotThrow(() -> mergeService.videos(inputFiles, outputFile));
            verify(mergeService).executeFfmpegCommand(any(ProcessBuilder.class));
        }

        @Test
        @DisplayName("FFMPEG_PATH가 유효하지 않을 때 INTERNAL_SERVER_ERROR 예외")
        void testVideos_InvalidFfmpegPath() {
            // given
            System.setProperty("FFMPEG_PATH", ""); // 빈 값(잘못된 경로)
            List<String> inputFiles = Collections.singletonList("/tmp/video1.mp4");
            String outputFile = "/tmp/output.mp4";

            // when
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    mergeService.videos(inputFiles, outputFile)
            );

            // then
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
            assertTrue(ex.getReason().contains("FFMPEG_PATH 환경 변수가 설정되지 않았습니다"));
        }
    }

    @Nested
    @DisplayName("imageAndAudio() 테스트")
    class ImageAndAudioTest {

        @Test
        @DisplayName("정상 케이스: 이미지와 오디오를 입력하면 예외 없이 성공")
        void testImageAndAudio_Success() {
            // given
            String imagePath = "/tmp/image.png";
            String audioPath = "/tmp/audio.mp3";
            String outputFile = "/tmp/image_audio.mp4";

            // when & then (실제 FFmpeg 호출은 doNothing() 처리되어 예외 발생하지 않음)
            assertDoesNotThrow(() -> mergeService.imageAndAudio(imagePath, audioPath, outputFile));
            verify(mergeService).executeFfmpegCommand(any(ProcessBuilder.class));
        }

        @Test
        @DisplayName("FFmpeg 실행 중 예외 발생 시 INTERNAL_SERVER_ERROR")
        void testImageAndAudio_FfmpegFailure() {
            // 실패 케이스: 잘못된 FFmpeg 경로를 설정하여 예외 발생 유도
            System.setProperty("FFMPEG_PATH", "/invalid/path/to/ffmpeg");

            // 이 경우에는 validateFfmpegPath()에서 예외가 발생하므로, executeFfmpegCommand는 호출되지 않음
            String imagePath = "/tmp/image.png";
            String audioPath = "/tmp/audio.mp3";
            String outputFile = "/tmp/image_audio.mp4";

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    mergeService.imageAndAudio(imagePath, audioPath, outputFile)
            );
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
            assertTrue(Objects.requireNonNull(ex.getReason()).contains("FFmpeg"));
        }
    }

    @Nested
    @DisplayName("createSectionVideos() 테스트")
    class CreateSectionVideosTest {

        @Test
        @DisplayName("섹션이 없는 경우 IllegalArgumentException 발생")
        void testCreateSectionVideos_EmptySections() {
            // given
            long projectId = 100L;
            when(sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId))
                    .thenReturn(Collections.emptyList());

            // when
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    mergeService.createSectionVideos(projectId)
            );

            // then
            assertTrue(ex.getMessage().contains("해당 프로젝트의 섹션을 찾을 수 없습니다"));
        }

        @Test
        @DisplayName("섹션이 있을 경우 정상 처리 - Supabase 업로드 호출 검증")
        void testCreateSectionVideos_Success() throws IOException {
            // given
            long projectId = 101L;

            Section section1 = new Section();
            section1.setId(1L);
            section1.setImageUrl("supabase://image1.png");
            section1.setTtsUrl("supabase://audio1.mp3");

            Section section2 = new Section();
            section2.setId(2L);
            section2.setImageUrl("supabase://image2.png");
            section2.setTtsUrl("supabase://audio2.mp3");

            when(sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId))
                    .thenReturn(List.of(section1, section2));

            // 가정: supabaseStorageService.downloadFile() 호출 시 로컬 임시 파일을 반환하도록 설정
            File mockImageFile = new File("/tmp/mock_image.png");
            File mockAudioFile = new File("/tmp/mock_audio.mp3");
            when(supabaseStorageService.downloadFile("supabase://image1.png"))
                    .thenReturn(mockImageFile);
            when(supabaseStorageService.downloadFile("supabase://audio1.mp3"))
                    .thenReturn(mockAudioFile);
            when(supabaseStorageService.downloadFile("supabase://image2.png"))
                    .thenReturn(mockImageFile);
            when(supabaseStorageService.downloadFile("supabase://audio2.mp3"))
                    .thenReturn(mockAudioFile);

            // Supabase 업로드도 모킹하여, 성공적으로 업로드된 URL을 반환하도록 설정
            when(supabaseStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("supabase://uploaded_video.mp4");

            // when
            mergeService.createSectionVideos(projectId);

            // then
            verify(sectionRepository, times(2)).save(any(Section.class));
            verify(supabaseStorageService, times(2)).uploadFile(any(MultipartFile.class));
            verify(mergeService, atLeastOnce()).executeFfmpegCommand(any(ProcessBuilder.class));
        }
    }
}