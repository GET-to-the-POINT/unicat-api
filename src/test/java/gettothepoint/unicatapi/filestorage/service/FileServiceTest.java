package gettothepoint.unicatapi.filestorage.service;

import gettothepoint.unicatapi.filestorage.FileResource;
import gettothepoint.unicatapi.filestorage.FileService;
import gettothepoint.unicatapi.filestorage.persistence.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 서비스 실패 케이스 테스트")
class FileServiceTest {

    @Mock
    private FileStorageRepository fileStorageRepository;

    @InjectMocks
    private FileService fileService;

    @Nested
    @DisplayName("파일 다운로드 테스트")
    class DownloadFileTest {

        @Test
        @DisplayName("파일키가 빈 문자열이면 예외가 발생한다")
        void emptyKeyThrowsException() {
            // given
            String emptyKey = "";

            // when & then
            assertThatThrownBy(() -> fileService.downloadFile(emptyKey))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 다운로드 키입니다");
        }

        @Test
        @DisplayName("파일키가 공백 문자열이면 예외가 발생한다")
        void blankKeyThrowsException() {
            // given
            String blankKey = "   ";

            // when & then
            assertThatThrownBy(() -> fileService.downloadFile(blankKey))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 다운로드 키입니다");
        }
    }

    @Nested
    @DisplayName("MultipartFile 업로드 테스트")
    class UploadMultipartFileTest {

        @Test
        @DisplayName("빈 MultipartFile을 업로드하면 예외가 발생한다")
        void emptyFileThrowsException() {
            // given
            MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

            // when and then
            assertThatThrownBy(() -> fileService.uploadFile(emptyFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("빈 파일은 업로드할 수 없습니다");
        }

    }

    @Nested
    @DisplayName("File 업로드 테스트")
    class UploadFileTest {

        @TempDir
        Path tempDir;
        
        @Test
        @DisplayName("존재하지 않는 파일을 업로드하면 예외가 발생한다")
        void nonExistentFileThrowsException() {
            // given
            File nonExistentFile = new File(tempDir.toFile(), "non-existent.txt");

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(nonExistentFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 파일입니다");
        }

        @Test
        @DisplayName("빈 파일을 업로드하면 예외가 발생한다")
        void emptyFileThrowsException() throws IOException {
            // given
            File emptyFile = Files.createFile(tempDir.resolve("empty.txt")).toFile();

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(emptyFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 파일입니다");
        }

        @Test
        @DisplayName("읽을 수 없는 파일을 업로드하면 예외가 발생한다")
        void unreadableFileThrowsException() throws IOException {
            // given
            File unreadableFile = Files.createFile(tempDir.resolve("unreadable.txt")).toFile();
            unreadableFile.setReadable(false);

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(unreadableFile))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 파일입니다");
        }
    }
    
    @Nested
    @DisplayName("저장소 관련 예외 테스트")
    class RepositoryExceptionTest {
        
        @Test
        @DisplayName("저장소에서 예외가 발생하면 그대로 전파된다")
        void repositoryExceptionIsPropagated() {
            // given
            MultipartFile validFile = new MockMultipartFile("file", "filename.txt", "text/plain", "content".getBytes());
            when(fileStorageRepository.store(any(FileResource.class))).thenThrow(new RuntimeException("저장소 오류"));

            // when & then
            assertThatThrownBy(() -> fileService.uploadFile(validFile))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("저장소 오류");
        }
    }
}
