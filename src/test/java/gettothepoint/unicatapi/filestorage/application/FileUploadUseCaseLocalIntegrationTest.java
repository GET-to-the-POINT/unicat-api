package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Path;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        FileUploadUseCase.class,
        CompositeFileStorageConfig.class,
        LocalFileStorageConfig.class,
})
@DisplayName("파일 업로드 유스케이스 통합 테스트")
class FileUploadUseCaseLocalIntegrationTest {

    @Autowired
    private FileUploadUseCase fileUploadUseCase;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }

    @Test
    @DisplayName("파일 업로드 성공 테스트")
    void uploadFileIntegrationShouldSucceed() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                TEST_FILENAME,
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                TEST_CONTENT.getBytes()
        );

        String fileKey = fileUploadUseCase.uploadFile(mockFile);

        assertNotNull(fileKey);

        Resource resource = fileStorageRepository.load(fileKey).orElseThrow(() ->
            new AssertionError("파일이 저장소에서 로드되지 않았습니다.")
        );
        String loadedContent = new String(resource.getInputStream().readAllBytes());
        assertEquals(TEST_CONTENT, loadedContent);
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생 테스트")
    void uploadFileIntegrationShouldFailWhenFileIsEmpty() {
        MockMultipartFile mockFile = new MockMultipartFile(
                TEST_FILENAME,
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("업로드할 파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("I/O 예외 발생 시 런타임 예외 발생 테스트")
    void uploadFileIntegrationShouldFailWhenIOExceptionOccurs() {
        MockMultipartFile mockFile = new MockMultipartFile(
                TEST_FILENAME,
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                (byte[]) null // I/O 오류를 유발
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });
    }

    @Test
    @DisplayName("파일 업로드 실패 테스트 - 확장자 조작 (허용되지 않는 확장자)")
    void uploadFileIntegrationShouldFailWithDisallowedExtension() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "malicious.sh",
                "malicious.sh",
                "application/x-sh",
                TEST_CONTENT.getBytes()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("파일의 MIME 타입이 일치하지 않습니다.", exception.getMessage());
    }
}
