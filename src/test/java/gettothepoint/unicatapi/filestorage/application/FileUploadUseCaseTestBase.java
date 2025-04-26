package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 파일 업로드 유스케이스 테스트를 위한 추상 클래스
 * 공통 테스트 로직을 정의하여 중복 코드를 제거합니다.
 */
public abstract class FileUploadUseCaseTestBase {

    @Autowired
    protected FileUploadUseCase fileUploadUseCase;

    @Autowired
    protected FileStorageRepository fileStorageRepository;

    /**
     * 테스트용 MockMultipartFile 생성 유틸리티 메서드
     */
    protected MockMultipartFile createMockFile(String filename, String contentType, byte[] content) {
        return new MockMultipartFile(
                filename,
                filename,
                contentType,
                content
        );
    }

    /**
     * 테스트용 MockMultipartFile 생성 유틸리티 메서드 (기본 테스트 데이터 사용)
     */
    protected MockMultipartFile createDefaultMockFile() {
        return createMockFile(
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                TEST_CONTENT.getBytes()
        );
    }

    /**
     * 빈 파일 생성 유틸리티 메서드
     */
    protected MockMultipartFile createEmptyMockFile() {
        return createMockFile(
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                new byte[0]
        );
    }

    /**
     * I/O 예외를 발생시키는 파일 생성 유틸리티 메서드
     */
    protected MockMultipartFile createIOErrorMockFile() {
        return createMockFile(
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                null
        );
    }

    /**
     * 허용되지 않는 확장자를 가진 파일 생성 유틸리티 메서드
     */
    protected MockMultipartFile createDisallowedExtensionMockFile() {
        return createMockFile(
                "malicious.sh",
                "application/x-sh",
                TEST_CONTENT.getBytes()
        );
    }

    /**
     * 저장된 파일의 내용 검증 유틸리티 메서드
     */
    protected void verifyFileContent(String fileKey, String expectedContent) throws IOException {
        Resource resource = fileStorageRepository.load(fileKey).orElseThrow(() ->
                new AssertionError("파일이 저장소에서 로드되지 않았습니다.")
        );
        String loadedContent = new String(resource.getInputStream().readAllBytes());
        assertEquals(expectedContent, loadedContent, "파일 내용이 일치하지 않습니다");
    }

    @Test
    @DisplayName("파일 업로드 성공 테스트")
    void uploadFileIntegrationShouldSucceed() throws IOException {
        // Given
        MockMultipartFile mockFile = createDefaultMockFile();

        // When
        String fileKey = fileUploadUseCase.uploadFile(mockFile);

        // Then
        assertNotNull(fileKey, "파일 키가 null입니다");
        verifyFileContent(fileKey, TEST_CONTENT);
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생 테스트")
    void uploadFileIntegrationShouldFailWhenFileIsEmpty() {
        // Given
        MockMultipartFile mockFile = createEmptyMockFile();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("업로드할 파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("I/O 예외 발생 시 런타임 예외 발생 테스트")
    void uploadFileIntegrationShouldFailWhenIOExceptionOccurs() {
        // Given
        MockMultipartFile mockFile = createIOErrorMockFile();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });
    }

    @Test
    @DisplayName("파일 업로드 실패 테스트 - 확장자 조작 (허용되지 않는 확장자)")
    void uploadFileIntegrationShouldFailWithDisallowedExtension() {
        // Given
        MockMultipartFile mockFile = createDisallowedExtensionMockFile();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("파일의 MIME 타입이 일치하지 않습니다.", exception.getMessage());
    }
}
