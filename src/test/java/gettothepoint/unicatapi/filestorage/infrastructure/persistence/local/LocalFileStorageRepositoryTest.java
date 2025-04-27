package gettothepoint.unicatapi.filestorage.infrastructure.persistence.local;

import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import gettothepoint.unicatapi.filestorage.infrastructure.command.StoredFileFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.mockStatic;

@DisplayName("로컬 파일 저장소 단위 테스트")
class LocalFileStorageRepositoryTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LocalFileStorageRepository(tempDir);
    }

    @Test
    @DisplayName("MultipartFile을 사용하여 파일 저장 및 로드 테스트")
    void storeAndLoadWithMultipartFile() throws IOException {
        // Given
        String content = "테스트 파일 내용입니다";
        String originalFilename = "original-file.txt";
        String expectedFilename = UUID.randomUUID() + ".txt";

        // MultipartFile 목 생성
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        // StoredFile 목 생성
        StoredFile storedFile = mock(StoredFile.class);
        given(storedFile.filename()).willReturn(expectedFilename);
        given(storedFile.content()).willReturn(
                new MockMultipartFile("file", expectedFilename, "text/plain",
                        content.getBytes(StandardCharsets.UTF_8)).getInputStream()
        );

        // StoredFileFactory 모킹
        try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
            factory.when(() -> StoredFileFactory.fromMultipartFile(eq(multipartFile), any(Path.class)))
                    .thenReturn(storedFile);

            // When
            String key = repository.store(storedFile);

            // Then
            assertThat(key).isEqualTo(expectedFilename);

            // 파일이 실제로 생성되었는지 확인
            Path savedFilePath = tempDir.resolve(expectedFilename);
            assertThat(Files.exists(savedFilePath)).isTrue();

            // 파일 내용이 올바른지 확인
            String savedContent = Files.readString(savedFilePath);
            assertThat(savedContent).isEqualTo(content);

            // load 메서드를 통해 파일 리소스 가져오기
            Optional<UrlResource> resourceOpt = repository.load(key);

            // 리소스가 존재하는지 확인
            assertThat(resourceOpt).isPresent();

            // 리소스 URL이 올바른지 확인
            UrlResource resource = resourceOpt.get();
            assertThat(resource.getURL().getProtocol()).isEqualTo("file");
            assertThat(resource.getFilename()).isEqualTo(expectedFilename);
            assertThat(resource.contentLength()).isEqualTo(content.getBytes(StandardCharsets.UTF_8).length);
        }
    }

    @Test
    @DisplayName("여러 MultipartFile 파일 저장 및 로드")
    void storeAndLoadMultipleFiles() throws IOException {
        // Given
        int fileCount = 3;
        String[] filenames = new String[fileCount];
        String[] contents = new String[fileCount];
        StoredFile[] storedFiles = new StoredFile[fileCount];

        for (int i = 0; i < fileCount; i++) {
            filenames[i] = "file-" + i + "-" + UUID.randomUUID() + ".txt";
            contents[i] = "파일 " + i + "의 내용입니다.";

            // StoredFile 목 생성
            storedFiles[i] = mock(StoredFile.class);
            given(storedFiles[i].filename()).willReturn(filenames[i]);
            given(storedFiles[i].content()).willReturn(
                    new MockMultipartFile("file", filenames[i], "text/plain",
                            contents[i].getBytes(StandardCharsets.UTF_8)).getInputStream()
            );

            // When - 각 파일 저장
            String key = repository.store(storedFiles[i]);

            // Then - 각 파일 검증
            assertThat(key).isEqualTo(filenames[i]);
            assertThat(Files.exists(tempDir.resolve(filenames[i]))).isTrue();

            // 파일 내용 검증
            String savedContent = Files.readString(tempDir.resolve(filenames[i]));
            assertThat(savedContent).isEqualTo(contents[i]);

            // 파일 로드 검증
            Optional<UrlResource> resourceOpt = repository.load(key);
            assertThat(resourceOpt).isPresent();
            assertThat(resourceOpt.get().getFilename()).isEqualTo(filenames[i]);
        }
    }

    @Test
    @DisplayName("존재하지 않는 파일 로드 시 빈 Optional 반환")
    void loadNonExistentFile() {
        // Given
        String nonExistentKey = "non-existent-file.txt";

        // When
        Optional<UrlResource> result = repository.load(nonExistentKey);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("하위 디렉토리 구조 파일 저장 및 로드")
    void storeAndLoadFileWithSubdirectories() throws IOException {
        // Given
        String content = "하위 디렉토리 테스트 내용";
        String subdirectoryPath = "subdir1/subdir2/";
        String filename = subdirectoryPath + UUID.randomUUID() + ".txt";

        // StoredFile 목 생성
        StoredFile storedFile = mock(StoredFile.class);
        given(storedFile.filename()).willReturn(filename);
        given(storedFile.content()).willReturn(
                new MockMultipartFile("file", filename, "text/plain",
                        content.getBytes(StandardCharsets.UTF_8)).getInputStream()
        );

        // When
        String key = repository.store(storedFile);

        // Then
        assertThat(key).isEqualTo(filename);

        // 파일이 실제로 생성되었는지 확인
        Path savedFilePath = tempDir.resolve(filename);
        assertThat(Files.exists(savedFilePath)).isTrue();

        // 디렉토리가 생성되었는지 확인
        Path subdirPath = tempDir.resolve(subdirectoryPath);
        assertThat(Files.exists(subdirPath)).isTrue();
        assertThat(Files.isDirectory(subdirPath)).isTrue();

        // load 메서드를 통해 파일 리소스 가져오기
        Optional<UrlResource> resourceOpt = repository.load(key);

        // 리소스가 존재하고 올바른지 확인
        assertThat(resourceOpt).isPresent();
        UrlResource resource = resourceOpt.get();
        assertThat(resource.getFilename()).endsWith(filename.substring(filename.lastIndexOf('/')+1));
    }

    @Test
    @DisplayName("대용량 파일 처리 테스트")
    void handleLargeFile() throws IOException {
        // Given - 1MB 크기의 파일 생성
        int fileSize = 1024 * 1024; // 1MB
        byte[] largeContent = new byte[fileSize];
        for (int i = 0; i < fileSize; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        String filename = "large-file.bin";

        // MultipartFile 목 생성
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                filename,
                "application/octet-stream",
                largeContent
        );

        // StoredFile 목 생성
        StoredFile storedFile = mock(StoredFile.class);
        given(storedFile.filename()).willReturn(filename);
        given(storedFile.content()).willReturn(multipartFile.getInputStream());

        // When
        String key = repository.store(storedFile);

        // Then
        assertThat(key).isEqualTo(filename);

        // 파일 크기 확인
        Path savedFilePath = tempDir.resolve(filename);
        assertThat(Files.size(savedFilePath)).isEqualTo(fileSize);

        // 리소스 로드 확인
        Optional<UrlResource> resourceOpt = repository.load(key);
        assertThat(resourceOpt).isPresent();
        assertThat(resourceOpt.get().contentLength()).isEqualTo(fileSize);
    }

    @Test
    @DisplayName("파일 입출력 예외 처리 테스트")
    void handleIOExceptions() {
        // Given
        String filename = "error-file.txt";

        // 입출력 예외를 발생시키는 InputStream 생성
        InputStream errorStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("강제 입출력 예외");
            }
        };

        // StoredFile 목 생성
        StoredFile storedFile = mock(StoredFile.class);
        given(storedFile.filename()).willReturn(filename);
        given(storedFile.content()).willReturn(errorStream);

        // When & Then
        assertThatThrownBy(() -> repository.store(storedFile))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("파일 저장 중 오류 발생");
    }

    @Test
    @DisplayName("잘못된 루트 경로로 저장소 초기화 시 예외 발생")
    void initializeWithInvalidRoot() {
        // Given
        Path invalidPath = Path.of("/this/path/should/not/exist/");

        // When & Then - 존재하지 않는 경로에 쓰기 권한이 없으면 예외 발생
        assertThatThrownBy(() -> new LocalFileStorageRepository(invalidPath))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("로컬 저장소 디렉토리 생성 실패");
    }

    @ParameterizedTest
    @DisplayName("특수 문자가 포함된 파일명 처리")
    @ValueSource(strings = {
            "special@#$%^&()-_=+[]{}.txt",
            "한글파일이름.txt",
            "mixed-한글-english.txt",
            "spaces in filename.txt"
    })
    void handleSpecialCharactersInFilename(String filename) throws IOException {
        // Given
        String content = "특수문자 파일명 테스트";

        // StoredFile 목 생성
        StoredFile storedFile = mock(StoredFile.class);
        given(storedFile.filename()).willReturn(filename);
        given(storedFile.content()).willReturn(
                new MockMultipartFile("file", filename, "text/plain",
                        content.getBytes(StandardCharsets.UTF_8)).getInputStream()
        );

        // When
        String key = repository.store(storedFile);

        // Then
        assertThat(key).isEqualTo(filename);

        // 파일이 실제로 생성되었는지 확인
        Path savedFilePath = tempDir.resolve(filename);
        assertThat(Files.exists(savedFilePath)).isTrue();

        // 로드 테스트
        Optional<UrlResource> resourceOpt = repository.load(key);
        assertThat(resourceOpt).isPresent();
    }
}