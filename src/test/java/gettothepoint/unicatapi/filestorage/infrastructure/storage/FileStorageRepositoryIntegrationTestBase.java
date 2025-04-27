package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import gettothepoint.unicatapi.filestorage.infrastructure.command.StoredFileFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 다양한 파일 저장소 구현체를 테스트하기 위한 공통 테스트 기반 클래스입니다.
 * 모든 파일 저장소 구현체는 이 클래스를 상속받아 공통 테스트를 수행해야 합니다.
 */
@DisplayName("공통 파일 저장소 테스트")
public abstract class FileStorageRepositoryIntegrationTestBase {

    // 상수 정의 영역
    private static final Random random = new Random();
    private static final int LARGE_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_CONTENT = "파일 테스트 내용";
    private static final String TEST_CONTENT_TYPE = "text/plain";
    private static final Path EMPTY_PATH = Path.of("");

    // ======= 추상 메서드 영역 (구현체에서 반드시 구현해야 함) =======

    /**
     * 테스트에 사용할 저장소 구현체를 반환하는 추상 메서드입니다.
     */
    protected abstract FileStorageRepository getRepository();

    /**
     * 예상되는 URL 프로토콜을 반환하는 추상 메서드입니다.
     */
    protected abstract String getExpectedUrlProtocol();

    /**
     * 프로토콜 검증에 대한 설명 메시지를 반환합니다.
     */
    protected abstract String getProtocolAssertionMessage();

    // ======= 공통 테스트 메서드 영역 =======

    @Test
    @DisplayName("로드된 파일의 프로토콜 확인 테스트")
    public void loadedFileShouldHaveCorrectScheme() {
        // Given: 테스트용 파일을 준비합니다
        String filename = "protocol-test-" + UUID.randomUUID() + ".txt";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", filename, TEST_CONTENT_TYPE,
                TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

        StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);
        String key = getRepository().store(storedFile);

        // When: 저장된 파일을 로드합니다
        Optional<UrlResource> resource = getRepository().load(key);

        // Then: 프로토콜이 예상과 일치하는지 확인합니다
        assertTrue(resource.isPresent(), "파일을 찾을 수 없습니다");
        assertEquals(getExpectedUrlProtocol(), resource.get().getURL().getProtocol(),
                getProtocolAssertionMessage());
    }

    // ======= 중첩 테스트 클래스 영역 =======

    @Nested
    @DisplayName("파일 저장 테스트")
    class StoreFileTests {
        @Test
        @DisplayName("기본 파일 저장 성공")
        void storeBasicFileShouldSucceed() {
            // Given: 기본 테스트 파일을 준비합니다
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", TEST_FILENAME, TEST_CONTENT_TYPE,
                    TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);

            // When & Then: 파일 저장이 예외 없이 성공하는지 확인합니다
            assertDoesNotThrow(() -> {
                getRepository().store(storedFile);
            }, "기본 파일 저장 중 예상치 못한 예외가 발생했습니다");
        }

        @Test
        @DisplayName("대용량 파일 저장 성공")
        void storeLargeFileShouldSucceed() throws IOException {
            // Given: 5MB 크기의 대용량 파일을 준비합니다
            byte[] largeContent = new byte[LARGE_FILE_SIZE];
            random.nextBytes(largeContent); // 랜덤 데이터로 채웁니다

            MockMultipartFile largeMultipartFile = new MockMultipartFile(
                    "file", "large_file.txt", TEST_CONTENT_TYPE, largeContent
            );

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(largeMultipartFile, EMPTY_PATH);

            // When: 대용량 파일을 저장합니다
            String key = getRepository().store(storedFile);

            // Then: 저장이 성공하고 내용이 올바른지 확인합니다
            assertThat(key).isNotBlank().withFailMessage("생성된 파일 키가 비어있습니다");

            // 대용량 파일 로드 및 내용 확인
            Optional<UrlResource> resource = getRepository().load(key);
            assertThat(resource).isPresent().withFailMessage("저장된 대용량 파일을 찾을 수 없습니다");

            try {
                byte[] loadedContent = resource.get().getInputStream().readAllBytes();
                assertThat(loadedContent).isEqualTo(largeContent).withFailMessage("저장 및 로드된 파일 내용이 일치하지 않습니다");
            } catch (IOException e) {
                fail("대용량 파일 읽기 중 오류가 발생했습니다", e);
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
            // Given: 특수 문자가 포함된 파일명으로 테스트 파일을 준비합니다
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", filename, TEST_CONTENT_TYPE,
                    TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);

            // When: 파일을 저장합니다
            String key = getRepository().store(storedFile);

            // Then: 저장이 성공하고 내용이 올바른지 확인합니다
            assertThat(key).isNotBlank().withFailMessage("생성된 파일 키가 비어있습니다");

            // 내용 확인
            Optional<UrlResource> resource = getRepository().load(key);
            assertThat(resource).isPresent().withFailMessage("특수 문자가 포함된 파일명의 파일을 찾을 수 없습니다");

            String loadedContent = new String(resource.get().getInputStream().readAllBytes());
            assertThat(loadedContent).isEqualTo(TEST_CONTENT)
                    .withFailMessage("저장 및 로드된 특수 파일명 파일의 내용이 일치하지 않습니다");
        }
    }

    @Nested
    @DisplayName("파일 로드 테스트")
    class LoadFileTests {
        @Test
        @DisplayName("저장된 파일 로드 성공")
        void loadStoredFileShouldSucceed() throws IOException {
            // Given: 테스트 파일을 저장합니다
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", TEST_FILENAME, TEST_CONTENT_TYPE,
                    TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);
            String key = getRepository().store(storedFile);

            // When: 저장된 파일을 로드합니다
            Optional<UrlResource> resource = getRepository().load(key);

            // Then: 파일이 로드되고 내용이 일치하는지 확인합니다
            assertThat(resource).isPresent().withFailMessage("저장된 파일을 찾을 수 없습니다");

            String loadedContent = new String(resource.get().getInputStream().readAllBytes());
            assertThat(loadedContent).isEqualTo(TEST_CONTENT)
                    .withFailMessage("로드된 파일의 내용이 저장한 내용과 일치하지 않습니다");
        }

        @Test
        @DisplayName("존재하지 않는 파일 로드시 Optional.empty 반환")
        void loadNonExistentFileShouldReturnEmpty() {
            // When: 존재하지 않는 파일의 키로 로드를 시도합니다
            Optional<UrlResource> resource = getRepository().load("nonexistent-" + System.currentTimeMillis() + ".txt");

            // Then: 결과가 비어있는지 확인합니다
            assertThat(resource).isEmpty()
                    .withFailMessage("존재하지 않는 파일을 로드했을 때 빈 Optional이 반환되어야 합니다");
        }

        @Test
        @DisplayName("다른 파일명이 아닌 정확한 키로만 로드 가능")
        void loadShouldUsePreciseKey() {
            // Given: 테스트 파일을 저장하고 잘못된 키를 준비합니다
            String filename = "test_precise_key.txt";
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", filename, TEST_CONTENT_TYPE,
                    TEST_CONTENT.getBytes(StandardCharsets.UTF_8));

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);
            String key = getRepository().store(storedFile);
            String wrongKey = key + "-wrong";

            // When & Then: 정확한 키로는 로드되고, 잘못된 키로는 로드되지 않는지 확인합니다
            assertThat(getRepository().load(key)).isPresent()
                    .withFailMessage("정확한 키로 파일을 로드할 수 없습니다");

            assertThat(getRepository().load(wrongKey)).isEmpty()
                    .withFailMessage("잘못된 키로 파일이 로드되었습니다");
        }

        @Test
        @DisplayName("파일 내용 무결성 검증")
        void contentIntegrityShouldBeMaintained() throws IOException {
            // Given: 특수 문자와 이모지가 포함된 콘텐츠로 파일을 저장합니다
            String content = "특수 문자가 포함된 콘텐츠: !@#$%^&*()_+\nNewline과 이모지 😊 테스트";
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "integrity_test.txt", TEST_CONTENT_TYPE,
                    content.getBytes(StandardCharsets.UTF_8));

            StoredFile storedFile = StoredFileFactory.fromMultipartFile(multipartFile, EMPTY_PATH);
            String key = getRepository().store(storedFile);

            // When: 저장된 파일을 로드합니다
            Optional<UrlResource> resource = getRepository().load(key);

            // Then: 내용이 정확하게 유지되는지 확인합니다
            assertThat(resource).isPresent()
                    .withFailMessage("무결성 테스트 파일을 찾을 수 없습니다");

            String loadedContent = new String(resource.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(loadedContent).isEqualTo(content)
                    .withFailMessage("특수 문자와 이모지가 포함된 파일의 내용이 손상되었습니다");
        }
    }
}