package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.config.LocalTestConfig;
import gettothepoint.unicatapi.filestorage.config.MinioFileStorageConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CompositeFileStorageConfig.class,
        MinioFileStorageConfig.class,
        LocalFileStorageConfig.class,
        LocalTestConfig.class,
})
@DisplayName("컴포지트 파일 저장소 테스트")
class CompositeFileStorageRepositoryTest {

    @Autowired
    private FileStorageRepository repository;

    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_CONTENT = "hello world";
    private static final String TEST_CONTENT_TYPE = "text/plain";

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
}
