package gettothepoint.unicatapi.filestorage.infrastructure.storage.local;

import gettothepoint.unicatapi.filestorage.config.LocalTestConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        LocalFileStorageConfig.class,
        LocalTestConfig.class,
})
@DisplayName("로컬 파일 저장소 테스트")
class LocalFileStorageRepositoryIntegrationTest {

    @Autowired
    private FileStorageRepository repository;

    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }

    @Test
    @DisplayName("파일 저장 성공 테스트")
    void storeFileShouldSucceed() {
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);
        assertEquals(TEST_FILENAME, key);
    }

    @Test
    @DisplayName("경로 조작 시도 시 파일 저장 실패 테스트")
    void storeFileShouldFailWithPathTraversal() {
        String maliciousFilename = "../malicious.txt";
        FileStorageCommand command = new FileStorageCommand(
                maliciousFilename,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        assertThrows(IllegalArgumentException.class, () -> repository.store(command));
    }

    @Test
    @DisplayName("저장된 파일 로드 성공 테스트")
    void loadExistingFileShouldSucceed() throws IOException {
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);

        Optional<UrlResource> resource = repository.load(key);

        assertTrue(resource.isPresent());
        String loadedContent = new String(resource.get().getInputStream().readAllBytes());
        assertEquals(TEST_CONTENT, loadedContent);
    }

    @Test
    @DisplayName("존재하지 않는 파일 로드 실패 테스트")
    void loadNonExistentFileShouldReturnEmpty() {
        Optional<UrlResource> resource = repository.load("nonexistent.txt");
        assertFalse(resource.isPresent());
    }

    @Test
    @DisplayName("로드된 파일의 스킴이 file로 시작해야 함")
    void loadedFileShouldHaveFileScheme() {
        FileStorageCommand command = new FileStorageCommand(
                TEST_FILENAME,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);

        Optional<UrlResource> resource = repository.load(key);

        assertTrue(resource.isPresent());
        assertEquals("file", resource.get().getURL().getProtocol());
    }

    @Test
    @DisplayName("빈 파일 저장 후 로드 테스트")
    void loadEmptyFileShouldSucceed() throws IOException {
        String emptyContent = "";
        FileStorageCommand command = new FileStorageCommand(
                "empty.txt",
                new ByteArrayInputStream(emptyContent.getBytes()),
                emptyContent.getBytes().length,
                "text/plain"
        );
        String key = repository.store(command);

        Optional<UrlResource> resource = repository.load(key);

        assertTrue(resource.isPresent());
        String loadedContent = new String(resource.get().getInputStream().readAllBytes());
        assertEquals(emptyContent, loadedContent);
    }

    @Test
    @DisplayName("특수 문자 파일 이름 저장 및 로드 테스트")
    void loadFileWithSpecialCharactersInNameShouldSucceed() throws IOException {
        String specialFilename = "특수문자_파일@이름!.txt";
        FileStorageCommand command = new FileStorageCommand(
                specialFilename,
                new ByteArrayInputStream(TEST_CONTENT.getBytes()),
                TEST_CONTENT.getBytes().length,
                TEST_CONTENT_TYPE
        );
        String key = repository.store(command);

        Optional<UrlResource> resource = repository.load(key);

        assertTrue(resource.isPresent());
        String loadedContent = new String(resource.get().getInputStream().readAllBytes());
        assertEquals(TEST_CONTENT, loadedContent);
    }

}
