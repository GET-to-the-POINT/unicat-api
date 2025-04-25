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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MinioFileStorageConfig.class, MinioTestConfig.class})
@DisplayName("Minio 파일 저장소 테스트")
@ActiveProfiles("dev") // dev, prod 모드에서만 테스트 실행
@Testcontainers
class MinioFileStorageRepositoryIntegrationTest {

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest");

    @Autowired
    FileStorageRepository repository;

    @DynamicPropertySource
    static void overrideMailProps(DynamicPropertyRegistry registry) {
        minio.start();
        registry.add("app.minio.bucket", () -> "test-bucket");
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }

    @Test
    @DisplayName("파일 저장 성공 테스트")
    void storeFileShouldSucceed() {
        // Given
        FileStorageCommand command = new FileStorageCommand(TEST_FILENAME, new ByteArrayInputStream(TEST_CONTENT.getBytes()), TEST_CONTENT.getBytes().length, TEST_CONTENT_TYPE);

        // When
        String key = repository.store(command);

        // Then
        assertEquals(TEST_FILENAME, key);
    }

    @Test
    @DisplayName("파일 저장 실패 테스트")
    void storeFileShouldFailWithInvalidBucket() {
        String maliciousFilename = "../malicious.txt";
        FileStorageCommand command = new FileStorageCommand(maliciousFilename, new ByteArrayInputStream(TEST_CONTENT.getBytes()), TEST_CONTENT.getBytes().length, TEST_CONTENT_TYPE);

        // When
        RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> repository.store(command));
        assertTrue(exception.getMessage().contains("잘못된 경로"));
    }

    @Test
    @DisplayName("저장된 파일 로드 성공 테스트")
    void loadExistingFileShouldSucceed() {
        // Given
        // First store the file
        FileStorageCommand command = new FileStorageCommand(TEST_FILENAME, new ByteArrayInputStream(TEST_CONTENT.getBytes()), TEST_CONTENT.getBytes().length, TEST_CONTENT_TYPE);
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

    @Test
    @DisplayName("로드된 파일의 스킴이 http로 시작해야 함")
    void loadedFileShouldHaveHttpScheme() {
        // Given
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);

        // When
        Optional<UrlResource> resource = repository.load(key);

        // Then
        assertTrue(resource.isPresent());
        assertEquals("http", resource.get().getURL().getProtocol());
    }

    @Test
    @DisplayName("빈 파일 저장 후 로드 테스트")
    void loadEmptyFileShouldSucceed() throws Exception {
        // Given
        String emptyContent = "";
        FileStorageCommand command = new FileStorageCommand(
                "empty.txt",
                new ByteArrayInputStream(emptyContent.getBytes()),
                emptyContent.getBytes().length,
                "text/plain"
        );
        String key = repository.store(command);

        // When
        Optional<UrlResource> resource = repository.load(key);

        // Then
        assertTrue(resource.isPresent());
        String loadedContent = new String(resource.get().getInputStream().readAllBytes());
        assertEquals(emptyContent, loadedContent);
    }

    @Test
    @DisplayName("특수 문자 파일 이름 저장 및 로드 테스트")
    void loadFileWithSpecialCharactersInNameShouldSucceed() throws Exception {
        // Given
        String specialFilename = "특수문자_파일@이름!.txt";
        FileStorageCommand command = new FileStorageCommand(
                specialFilename,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);

        // When
        Optional<UrlResource> resource = repository.load(key);

        // Then
        assertTrue(resource.isPresent());
        String loadedContent = new String(resource.get().getInputStream().readAllBytes());
        assertEquals(TEST_CONTENT, loadedContent);
    }
}
