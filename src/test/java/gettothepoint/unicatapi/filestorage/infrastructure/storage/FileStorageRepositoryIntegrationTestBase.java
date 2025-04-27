package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.DefaultFileStorageCommandConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 다양한 파일 저장소 구현체를 테스트하기 위한 공통 테스트 기반 클래스입니다.
 * 모든 파일 저장소 구현체는 이 클래스를 상속받아 공통 테스트를 수행해야 합니다.
 */
@DisplayName("공통 파일 저장소 테스트")
@SpringJUnitConfig(classes = {
        DefaultFileStorageCommandConfig.class,
        DefaultFileStorageCommandValidator.class,
        DefaultFileNameTransformer.class
})
public abstract class FileStorageRepositoryIntegrationTestBase {

    // 상수 정의 영역
    private static final Random random = new Random();
    private static final int LARGE_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    protected FileStorageRepository repository;

    // ======= 추상 메서드 영역 (구현체에서 반드시 구현해야 함) =======
    
    /**
     * 테스트에 사용할 저장소 구현체를 반환하는 추상 메서드입니다.
     * 각 구현체는 이 메서드를 통해 자신의 저장소 인스턴스를 제공해야 합니다.
     * 
     * @return 테스트할 파일 저장소 구현체
     */
    protected abstract FileStorageRepository getRepository();

    /**
     * 예상되는 URL 프로토콜을 반환하는 추상 메서드입니다.
     * 각 구현체에서 이 메서드를 구현하여 프로토콜 검증에 사용합니다.
     * (예: "file", "s3", "http" 등)
     * 
     * @return 예상되는 URL 프로토콜 문자열
     */
    protected abstract String getExpectedUrlProtocol();

    /**
     * 프로토콜 검증에 대한 설명 메시지를 반환합니다.
     * 테스트 실패 시 이 메시지가 출력되어 구체적인 문제를 파악하는데 도움을 줍니다.
     * 
     * @return 프로토콜 검증 실패 시 표시할 메시지
     */
    protected abstract String getProtocolAssertionMessage();

    // ======= 공통 테스트 메서드 영역 =======

    /**
     * 로드된 파일의 프로토콜이 각 구현체에서 기대하는 것과 일치하는지 확인합니다.
     * 이 테스트는 모든 저장소 구현체에서 동일하게 수행됩니다.
     */
    @Test
    @DisplayName("로드된 파일의 프로토콜 확인 테스트")
    public void loadedFileShouldHaveCorrectScheme() {
        // Given: 테스트용 파일을 준비합니다
        String filename = "protocol-test-" + UUID.randomUUID() + ".txt";
        FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
        String key = getRepository().store(command);

        // When: 저장된 파일을 로드합니다
        Optional<UrlResource> resource = getRepository().load(key);

        // Then: 프로토콜이 예상과 일치하는지 확인합니다
        assertTrue(resource.isPresent(), "파일을 찾을 수 없습니다");
        assertEquals(getExpectedUrlProtocol(), resource.get().getURL().getProtocol(),
                getProtocolAssertionMessage());
    }

    // ======= 중첩 테스트 클래스 영역 =======

    /**
     * 파일 저장 기능에 대한 다양한 테스트 케이스들을 모아놓은 중첩 클래스입니다.
     * 기본 파일, 대용량 파일, 특수 문자가 포함된 파일명 등 다양한 상황을 테스트합니다.
     */
    @Nested
    @DisplayName("파일 저장 테스트")
    class StoreFileTests {
        @Test
        @DisplayName("기본 파일 저장 성공")
        void storeBasicFileShouldSucceed() {
            // Given: 기본 테스트 파일을 준비합니다
            FileStorageCommand command = createTestFileCommand(TEST_FILENAME, TEST_CONTENT);

            // When & Then: 파일 저장이 예외 없이 성공하는지 확인합니다
            assertDoesNotThrow(() -> {
                repository.store(command);
            }, "기본 파일 저장 중 예상치 못한 예외가 발생했습니다");
        }

        @Test
        @DisplayName("대용량 파일 저장 성공")
        void storeLargeFileShouldSucceed() {
            // Given: 5MB 크기의 대용량 파일을 준비합니다
            byte[] largeContent = new byte[LARGE_FILE_SIZE];
            random.nextBytes(largeContent); // 랜덤 데이터로 채웁니다



            FileStorageCommand command = DefaultFileStorageCommand.builder()
            .filename("large_file.txt")
            .content(new ByteArrayInputStream(largeContent))
            .size(LARGE_FILE_SIZE)
            .contentType("text/plain")
            .build();

            // When: 대용량 파일을 저장합니다
            String key = repository.store(command);

            // Then: 저장이 성공하고 내용이 올바른지 확인합니다
            assertThat(key).isNotBlank().withFailMessage("생성된 파일 키가 비어있습니다");

            // 대용량 파일 로드 및 내용 확인
            Optional<UrlResource> resource = repository.load(key);
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
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);

            // When: 파일을 저장합니다
            String key = repository.store(command);

            // Then: 저장이 성공하고 내용이 올바른지 확인합니다
            assertThat(key).isNotBlank().withFailMessage("생성된 파일 키가 비어있습니다");

            // 내용 확인
            Optional<UrlResource> resource = repository.load(key);
            assertThat(resource).isPresent().withFailMessage("특수 문자가 포함된 파일명의 파일을 찾을 수 없습니다");

            String loadedContent = new String(resource.get().getInputStream().readAllBytes());
            assertThat(loadedContent).isEqualTo(TEST_CONTENT)
                    .withFailMessage("저장 및 로드된 특수 파일명 파일의 내용이 일치하지 않습니다");
        }
    }

    /**
     * 파일 로드 기능에 대한 다양한 테스트 케이스들을 모아놓은 중첩 클래스입니다.
     * 정상 로드, 존재하지 않는 파일 로드 시도, 키 정확성, 내용 무결성 등을 테스트합니다.
     */
    @Nested
    @DisplayName("파일 로드 테스트")
    class LoadFileTests {
        @Test
        @DisplayName("저장된 파일 로드 성공")
        void loadStoredFileShouldSucceed() throws IOException {
            // Given: 테스트 파일을 저장합니다
            String key = storeTestFile(TEST_FILENAME, TEST_CONTENT);

            // When: 저장된 파일을 로드합니다
            Optional<UrlResource> resource = repository.load(key);

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
            Optional<UrlResource> resource = repository.load("nonexistent-" + System.currentTimeMillis() + ".txt");

            // Then: 결과가 비어있는지 확인합니다
            assertThat(resource).isEmpty()
                    .withFailMessage("존재하지 않는 파일을 로드했을 때 빈 Optional이 반환되어야 합니다");
        }

        @Test
        @DisplayName("다른 파일명이 아닌 정확한 키로만 로드 가능")
        void loadShouldUsePreciseKey() {
            // Given: 테스트 파일을 저장하고 잘못된 키를 준비합니다
            String filename = "test_precise_key.txt";
            String key = storeTestFile(filename, TEST_CONTENT);
            String wrongKey = key + "-wrong";

            // When & Then: 정확한 키로는 로드되고, 잘못된 키로는 로드되지 않는지 확인합니다
            assertThat(repository.load(key)).isPresent()
                    .withFailMessage("정확한 키로 파일을 로드할 수 없습니다");
            
            assertThat(repository.load(wrongKey)).isEmpty()
                    .withFailMessage("잘못된 키로 파일이 로드되었습니다");
        }

        @Test
        @DisplayName("파일 내용 무결성 검증")
        void contentIntegrityShouldBeMaintained() throws IOException {
            // Given: 특수 문자와 이모지가 포함된 콘텐츠로 파일을 저장합니다
            String content = "특수 문자가 포함된 콘텐츠: !@#$%^&*()_+\nNewline과 이모지 😊 테스트";
            String key = storeTestFile("integrity_test.txt", content);

            // When: 저장된 파일을 로드합니다
            Optional<UrlResource> resource = repository.load(key);

            // Then: 내용이 정확하게 유지되는지 확인합니다
            assertThat(resource).isPresent()
                    .withFailMessage("무결성 테스트 파일을 찾을 수 없습니다");
            
            String loadedContent = new String(resource.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(loadedContent).isEqualTo(content)
                    .withFailMessage("특수 문자와 이모지가 포함된 파일의 내용이 손상되었습니다");
        }
    }

    // ======= 유틸리티 메서드 영역 =======

    /**
     * 테스트용 파일을 저장하고 생성된 키를 반환하는 편의 메서드입니다.
     * 
     * @param filename 저장할 파일명
     * @param content 파일에 저장할 내용
     * @return 저장된 파일의 키
     */
    protected String storeTestFile(String filename, String content) {
        FileStorageCommand command = createTestFileCommand(filename, content);
        return repository.store(command);
    }

    /**
     * 테스트용 FileStorageCommand 객체를 생성하는 편의 메서드입니다.
     * 
     * @param filename 파일명
     * @param content 파일 내용
     * @return 생성된 FileStorageCommand 객체
     */
    protected FileStorageCommand createTestFileCommand(String filename, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return DefaultFileStorageCommand.builder()
                .filename(filename)
                .content(new ByteArrayInputStream(bytes))
                .size(bytes.length)
                .contentType(TEST_CONTENT_TYPE)
                .build();
    }
}
