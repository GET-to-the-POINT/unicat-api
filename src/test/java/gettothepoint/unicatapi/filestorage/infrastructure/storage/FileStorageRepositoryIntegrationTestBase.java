package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("공통 파일 저장소 테스트")
public abstract class FileStorageRepositoryIntegrationTestBase {

    @Autowired
    protected FileStorageRepository repository;

    private static final Random random = new Random();
    private static final int LARGE_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 예상되는 URL 프로토콜을 반환하는 추상 메서드
     * 각 구현체에서 이 메서드를 구현하여 프로토콜 검증에 사용
     */
    protected abstract String getExpectedUrlProtocol();

    /**
     * 프로토콜 검증에 대한 설명 메시지 반환
     */
    protected abstract String getProtocolAssertionMessage();

    /**
     * 로드된 파일의 프로토콜을 확인하는 공통 테스트
     */
    @Test
    @DisplayName("로드된 파일의 프로토콜 확인 테스트")
    public void loadedFileShouldHaveCorrectScheme() throws IOException {
        // Given
        String filename = "protocol-test-" + UUID.randomUUID() + ".txt";
        FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
        String key = getRepository().store(command);

        // When
        Optional<UrlResource> resource = getRepository().load(key);

        // Then
        assertTrue(resource.isPresent(), "파일을 찾을 수 없음");
        assertEquals(getExpectedUrlProtocol(), resource.get().getURL().getProtocol(),
                getProtocolAssertionMessage());
    }

    /**
     * 테스트에 사용할 저장소 구현체를 반환하는 추상 메서드
     */
    protected abstract gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository getRepository();

    @Nested
    @DisplayName("파일 저장 테스트")
    class StoreFileTests {
        @Test
        @DisplayName("기본 파일 저장 성공")
        void storeBasicFileShouldSucceed() {
            // Given
            FileStorageCommand command = createTestFileCommand(TEST_FILENAME, TEST_CONTENT);

            // When & Then
            assertDoesNotThrow(() -> {
                repository.store(command);
            });
        }

        @Test
        @DisplayName("대용량 파일 저장 성공")
        void storeLargeFileShouldSucceed() {
            // Given
            byte[] largeContent = new byte[LARGE_FILE_SIZE];
            random.nextBytes(largeContent);

            FileStorageCommand command = new FileStorageCommand(
                    "large_file.txt",
                    new ByteArrayInputStream(largeContent),
                    largeContent.length,
                    "text/plain"
            );

            // When
            String key = repository.store(command);

            // Then
            assertThat(key).isNotBlank();                       // 변환된 파일명(해시 포함)이어야 함

            // Verify large file can be loaded
            Optional<UrlResource> resource = repository.load(key);
            assertThat(resource).isPresent();

            try {
                byte[] loadedContent = resource.get().getInputStream().readAllBytes();
                assertThat(loadedContent).isEqualTo(largeContent);
            } catch (IOException e) {
                fail("Failed to read large file content", e);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "특수문자_파일@이름!.txt",
                "file with spaces.txt",
                "file-with-dashes.txt",
                "파일_한글_이름.txt",
                "symbols_#$%&.txt"
        })
        @DisplayName("다양한 파일명 저장 성공")
        void storeFilesWithSpecialCharactersInNameShouldSucceed(String filename) throws IOException {
            // Given
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);

            // When
            String key = repository.store(command);

            // Then
            assertThat(key).isNotBlank();                       // 해시가 포함된 새 파일명

            // Verify content
            Optional<UrlResource> resource = repository.load(key);
            assertThat(resource).isPresent();

            String loadedContent = new String(resource.get().getInputStream().readAllBytes());
            assertThat(loadedContent).isEqualTo(TEST_CONTENT);
        }
    }

    @Nested
    @DisplayName("파일 로드 테스트")
    class LoadFileTests {
        @Test
        @DisplayName("저장된 파일 로드 성공")
        void loadStoredFileShouldSucceed() throws IOException {
            // Given
            String key = storeTestFile(TEST_FILENAME, TEST_CONTENT);

            // When
            Optional<UrlResource> resource = repository.load(key);

            // Then
            assertThat(resource).isPresent();

            String loadedContent = new String(resource.get().getInputStream().readAllBytes());
            assertThat(loadedContent).isEqualTo(TEST_CONTENT);
        }

        @Test
        @DisplayName("존재하지 않는 파일 로드시 Optional.empty 반환")
        void loadNonExistentFileShouldReturnEmpty() {
            // When
            Optional<UrlResource> resource = repository.load("nonexistent-" + System.currentTimeMillis() + ".txt");

            // Then
            assertThat(resource).isEmpty();
        }

        @Test
        @DisplayName("다른 파일명이 아닌 정확한 키로만 로드 가능")
        void loadShouldUsePreciseKey() {
            // Given
            String filename = "test_precise_key.txt";
            String key = storeTestFile(filename, TEST_CONTENT);
            String wrongKey = key + "-wrong";

            // When/Then
            assertThat(repository.load(key)).isPresent();
            assertThat(repository.load(wrongKey)).isEmpty();
        }

        @Test
        @DisplayName("파일 내용 무결성 검증")
        void contentIntegrityShouldBeMaintained() throws IOException {
            // Given
            String content = "특수 문자가 포함된 콘텐츠: !@#$%^&*()_+\nNewline과 이모지 😊 테스트";
            String key = storeTestFile("integrity_test.txt", content);

            // When
            Optional<UrlResource> resource = repository.load(key);

            // Then
            assertThat(resource).isPresent();
            String loadedContent = new String(resource.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(loadedContent).isEqualTo(content);
        }
    }

    // 유틸리티 메서드
    protected String storeTestFile(String filename, String content) {
        FileStorageCommand command = createTestFileCommand(filename, content);
        return repository.store(command);
    }

    protected FileStorageCommand createTestFileCommand(String filename, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new FileStorageCommand(
                filename,
                new ByteArrayInputStream(bytes),
                bytes.length,
                TEST_CONTENT_TYPE
        );
    }

}
