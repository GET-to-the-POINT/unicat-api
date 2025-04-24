package gettothepoint.unicatapi.filestorage.application;// ...existing imports...

import gettothepoint.unicatapi.filestorage.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.config.LocalTestConfig;
import gettothepoint.unicatapi.filestorage.config.MinioFileStorageConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        FileUploadUseCase.class,
        CompositeFileStorageConfig.class,
        MinioFileStorageConfig.class,
        LocalFileStorageConfig.class,
        LocalTestConfig.class,
})
@DisplayName("파일 업로드 유스케이스 통합 테스트")
class FileUploadUseCaseIntegrationTest {

    @Autowired
    private FileUploadUseCase fileUploadUseCase;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_CONTENT = "hello world";
    private static final String TEST_CONTENT_TYPE = "text/plain";

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
