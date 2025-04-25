package gettothepoint.unicatapi.filestorage.infrastructure.storage.minio;

import gettothepoint.unicatapi.filestorage.config.MinioTestConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.MinioFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        MinioFileStorageConfig.class,
        MinioTestConfig.class
})
@DisplayName("Minio 파일 저장소 테스트")
@ActiveProfiles("dev") // dev, prod 모드에서만 테스트 실행
class MinioFileStorageRepositoryTest {

    @Autowired
    FileStorageRepository repository;

    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_CONTENT = "hello world";
    private static final String TEST_CONTENT_TYPE = "text/plain";

    @Test
    @DisplayName("파일 저장 성공 테스트")
    void storeFileShouldSucceed() {
        // Given
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );

        // When
        String key = repository.store(command);

        // Then
        assertEquals(TEST_FILENAME, key);
    }

    @Test
    @DisplayName("파일 저장 실패 테스트")
    void storeFileShouldFailWithInvalidBucket() {
        String maliciousFilename = "../malicious.txt";
        FileStorageCommand command = new FileStorageCommand(
                maliciousFilename,
            new ByteArrayInputStream(TEST_CONTENT.getBytes()),
            TEST_CONTENT.getBytes().length,
            TEST_CONTENT_TYPE
        );

        // When
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> repository.store(command));
        assertTrue(exception.getMessage().contains("잘못된 경로"));
    }

    @Test
    @DisplayName("저장된 파일 로드 성공 테스트")
    void loadExistingFileShouldSucceed() {
        // Given
        // First store the file
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        repository.store(command);

        // When
        Optional<UrlResource> resource = repository.load(TEST_FILENAME);

        // Then
        assertTrue(resource.isPresent(), "파일을 찾을 수 없음");
        assertTrue(resource.get().getURL().toString().contains(TEST_FILENAME));
    }

    @Test
    @DisplayName("파일 로드 실패 테스트")
    void loadFileShouldReturnEmptyWhenFileDoesNotExist() {
        // When
        Optional<UrlResource> resource = repository.load("non-existent-file.txt");

        // Then
        assertFalse(resource.isPresent(), "존재하지 않는 파일에 대해 결과가 반환됨");
    }
}
