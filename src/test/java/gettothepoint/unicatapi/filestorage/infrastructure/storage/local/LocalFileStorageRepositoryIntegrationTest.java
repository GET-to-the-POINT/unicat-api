package gettothepoint.unicatapi.filestorage.infrastructure.storage.local;

import gettothepoint.unicatapi.filestorage.config.LocalTestConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.UUID;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.TEST_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        LocalFileStorageConfig.class,
        LocalTestConfig.class,
})
@DisplayName("로컬 파일 저장소 테스트")
class LocalFileStorageRepositoryIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    @Autowired
    private FileStorageRepository repository;

    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }
    
    @Override
    protected String getExpectedUrlProtocol() {
        return "file";
    }
    
    @Override
    protected String getProtocolAssertionMessage() {
        return "로컬 파일 저장소는 file 프로토콜을 사용해야 함";
    }
    
    @Override
    protected FileStorageRepository getRepository() {
        return repository;
    }

    @Nested
    @DisplayName("로컬 저장소 특화 테스트")
    class LocalSpecificTests {
    
        @Test
        @DisplayName("로드된 파일의 프로토콜 확인")
        void loadedFileShouldHaveCorrectScheme() {
            // Given
            String filename = "protocol-test-" + UUID.randomUUID() + ".txt";
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
            String key = repository.store(command);

            // When
            Optional<UrlResource> resource = repository.load(key);

            // Then
            assertThat(resource).isPresent();
            assertThat(resource.get().getURL().getProtocol())
                    .as("로컬 파일 저장소는 file 프로토콜을 사용해야 함")
                    .isEqualTo("file");
        }
        
        @Test
        @DisplayName("파일 시스템에 실제 파일 존재 여부 확인")
        void fileShouldExistOnFileSystem() throws IOException {
            // Given
            String filename = "filesystem-test-" + UUID.randomUUID() + ".txt";
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
            
            // When
            String key = repository.store(command);
            
            // Then - 파일 시스템에서 직접 확인
            Path expectedPath = tempDir.resolve(key);
            assertThat(Files.exists(expectedPath))
                    .as("저장된 파일이 파일 시스템에 실제로 존재해야 함")
                    .isTrue();
            
            String directlyReadContent = Files.readString(expectedPath);
            assertThat(directlyReadContent).isEqualTo(TEST_CONTENT);
        }
        
        @Test
        @DisplayName("파일 시스템 메타데이터 검증")
        void fileSystemMetadataShouldBeCorrect() throws IOException {
            // Given
            String filename = "metadata-test-" + UUID.randomUUID() + ".txt";
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
            
            // When
            String key = repository.store(command);
            
            // Then - 파일 시스템 메타데이터 확인
            Path filePath = tempDir.resolve(key);
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            
            // 파일 크기 검증
            assertThat(attrs.size())
                    .as("파일 크기는 원본 콘텐츠 바이트 길이와 일치해야 함")
                    .isEqualTo(TEST_CONTENT.getBytes(StandardCharsets.UTF_8).length);
            
            // 파일 생성 시간 검증 (현재 시간과 비교하여 최근에 생성됨을 확인)
            long currentTimeMillis = System.currentTimeMillis();
            long fileCreationTimeMillis = attrs.creationTime().toMillis();
            
            assertThat(currentTimeMillis - fileCreationTimeMillis)
                    .as("파일은 최근에 생성되어야 함 (30초 이내)")
                    .isLessThan(30_000); // 30초 이내
        }
        
        @Test
        @DisplayName("중첩 디렉토리 구조 테스트")
        void nestedDirectoryStructureShouldWork() {
            // Given
            String nestedFilename = "nested/folder/structure/test.txt";
            FileStorageCommand command = createTestFileCommand(nestedFilename, TEST_CONTENT);
            
            // When
            String key = repository.store(command);
            
            // Then
            Optional<UrlResource> resource = repository.load(key);
            assertThat(resource).isPresent();
            
            // 파일 시스템에 경로 확인
            Path expectedPath = tempDir.resolve(key);
            assertThat(Files.exists(expectedPath))
                    .as("중첩된 디렉토리 구조의 파일이 생성되어야 함")
                    .isTrue();
        }
    }
}
