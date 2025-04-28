package gettothepoint.unicatapi.filestorage.infrastructure.persistence.minio;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.infrastructure.config.MinioFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = {MinioFileStorageConfig.class})
@ActiveProfiles("dev")
@DisplayName("Minio 파일 저장소 테스트")
@Testcontainers
class MinioFileStorageRepositoryTest {

    private static final String TEST_BUCKET_PREFIX = "test-bucket-";

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @DynamicPropertySource
    static void configureMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("app.minio.bucket", () -> TEST_BUCKET_PREFIX + UUID.randomUUID());
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }

    @Autowired
    private FileStorageRepository repository;

    @Nested
    @DisplayName("기본 파일 저장 및 로드 기능")
    class BasicFileOperations {

        @Test
        @DisplayName("텍스트 파일 저장 및 로드")
        void shouldStoreAndLoadTextFile() {
            // Given: 테스트 텍스트 파일
            String content = "미니오 파일 저장소 테스트 텍스트입니다";
            String filename = generateUniqueFilename(".txt");
            FileResource fileResource = createMockStoredFile(filename, content);

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key)
                    .as("저장 후 반환된 키는 원본 파일명과 일치해야 함")
                    .isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드 결과 검증
            assertThat(resourceOpt)
                    .as("저장된 파일이 로드되어야 함")
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            String loadedContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            assertThat(loadedContent)
                                    .as("로드된 파일 내용이 원본과 일치해야 함")
                                    .isEqualTo(content);
                        }
                    }));
        }

        @Test
        @DisplayName("바이너리 파일 저장 및 로드")
        void shouldStoreAndLoadBinaryFile() {
            // Given: 테스트 바이너리 파일
            byte[] binaryContent = new byte[1024]; 
            Arrays.fill(binaryContent, (byte)42);
            
            String filename = generateUniqueFilename(".bin");
            FileResource fileResource = createMockStoredFile(
                    filename, 
                    binaryContent, 
                    "application/octet-stream"
            );

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key).isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드 결과 검증
            assertThat(resourceOpt)
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            byte[] loadedContent = is.readAllBytes();
                            assertThat(loadedContent)
                                    .as("로드된 바이너리 내용이 원본과 일치해야 함")
                                    .isEqualTo(binaryContent);
                        }
                    }));
        }
    }

    @Nested
    @DisplayName("특수 케이스 처리")
    class SpecialCases {
        
        @Test
        @DisplayName("존재하지 않는 파일 로드")
        void shouldReturnEmptyOptionalForNonExistentFile() {
            // Given: 존재하지 않는 파일 키
            String nonExistentKey = "non-existent-file-" + UUID.randomUUID() + ".txt";

            // When: 파일 로드 시도
            Optional<UrlResource> result = repository.load(nonExistentKey);

            // Then: 빈 Optional 반환
            assertThat(result)
                    .as("존재하지 않는 파일은 빈 Optional을 반환해야 함")
                    .isEmpty();
        }

        @ParameterizedTest(name = "파일명: {0}")
        @DisplayName("특수 문자가 포함된 파일명 처리")
        @ValueSource(strings = {
                "special@#$%^&()-_=+[]{}.txt",
                "한글파일이름.txt",
                "mixed-한글-english.txt",
                "spaces in filename.txt"
        })
        void shouldHandleSpecialCharactersInFilename(String specialFilename) {
            // Given: 특수 문자가 포함된 파일명
            String content = "특수 파일명 테스트 내용";
            String filename = UUID.randomUUID() + "-" + specialFilename;
            FileResource fileResource = createMockStoredFile(filename, content);

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key).isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드 결과 검증
            assertThat(resourceOpt)
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            String loadedContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                            assertThat(loadedContent).isEqualTo(content);
                        }
                    }));
        }
    }

    @Nested
    @DisplayName("대용량 파일 및 성능 테스트")
    class LargeFileTests {

        @Test
        @DisplayName("대용량 파일 저장 및 로드")
        void shouldHandleLargeFile() {
            // Given: 10MB 크기의 파일 생성
            int fileSize = 10 * 1024 * 1024; // 10MB
            byte[] largeContent = new byte[fileSize];
            
            // 패턴화된 데이터로 채우기
            for (int i = 0; i < fileSize; i++) {
                largeContent[i] = (byte)(i % 256);
            }
            
            String filename = generateUniqueFilename(".dat");
            FileResource fileResource = createMockStoredFile(
                    filename, 
                    largeContent, 
                    "application/octet-stream"
            );

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key).isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드된 파일 검증
            assertThat(resourceOpt)
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            byte[] loadedContent = is.readAllBytes();

                            // 파일 크기 검증
                            assertThat(loadedContent)
                                    .as("로드된 파일의 크기가 원본과 일치해야 함")
                                    .hasSize(fileSize);

                            // 파일의 시작, 중간, 끝 부분 검증 (전체 비교는 시간이 많이 소요되므로 샘플링)
                            assertSoftly(softly -> {
                                // 시작 부분 (첫 1KB)
                                softly.assertThat(Arrays.copyOfRange(loadedContent, 0, 1024))
                                        .as("파일 시작 부분 (1KB) 내용 검증")
                                        .isEqualTo(Arrays.copyOfRange(largeContent, 0, 1024));

                                // 중간 부분 (5MB 지점 1KB)
                                int midPoint = fileSize / 2;
                                softly.assertThat(Arrays.copyOfRange(loadedContent, midPoint, midPoint + 1024))
                                        .as("파일 중간 부분 (5MB 지점 1KB) 내용 검증")
                                        .isEqualTo(Arrays.copyOfRange(largeContent, midPoint, midPoint + 1024));

                                // 끝 부분 (마지막 1KB)
                                softly.assertThat(Arrays.copyOfRange(loadedContent, fileSize - 1024, fileSize))
                                        .as("파일 끝 부분 (마지막 1KB) 내용 검증")
                                        .isEqualTo(Arrays.copyOfRange(largeContent, fileSize - 1024, fileSize));
                            });
                        }
                    }));
        }
        
        @ParameterizedTest(name = "{0}KB 파일 처리")
        @DisplayName("다양한 크기의 파일 저장 및 로드")
        @MethodSource("fileSizeProvider")
        void shouldHandleVariousFileSizes(int sizeInKB, String description) {
            // Given: 다양한 크기의 테스트 파일
            int fileSize = sizeInKB * 1024;
            byte[] content = new byte[fileSize];
            
            // 의미 있는 패턴으로 데이터 채우기
            for (int i = 0; i < fileSize; i++) {
                content[i] = (byte)((i * 7) % 256); // 단순 패턴보다 다양한 값 생성
            }
            
            String filename = generateUniqueFilename("-" + sizeInKB + "KB.dat");
            FileResource fileResource = createMockStoredFile(
                    filename, 
                    content, 
                    "application/octet-stream"
            );

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key).isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드된 파일 검증
            assertThat(resourceOpt)
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            byte[] loadedContent = is.readAllBytes();
                            assertThat(loadedContent)
                                    .as("%s 크기의 파일이 정확히 로드되어야 함", description)
                                    .hasSize(fileSize);

                            // 샘플링 검증 (전체 비교는 대용량 파일에서 시간이 많이 소요됨)
                            if (fileSize <= 1024 * 1024) { // 1MB 이하는 전체 비교
                                assertThat(loadedContent)
                                        .as("1MB 이하 파일은 전체 내용 검증")
                                        .isEqualTo(content);
                            } else { // 그 이상은 샘플링
                                assertSoftly(softly -> {
                                    // 시작, 중간, 끝 각 1KB 검증
                                    int checkSize = 1024;
                                    softly.assertThat(Arrays.copyOfRange(loadedContent, 0, checkSize))
                                            .as("파일 시작 부분 내용 검증")
                                            .isEqualTo(Arrays.copyOfRange(content, 0, checkSize));

                                    int midPoint = fileSize / 2;
                                    softly.assertThat(Arrays.copyOfRange(loadedContent, midPoint, midPoint + checkSize))
                                            .as("파일 중간 부분 내용 검증")
                                            .isEqualTo(Arrays.copyOfRange(content, midPoint, midPoint + checkSize));

                                    softly.assertThat(Arrays.copyOfRange(loadedContent, fileSize - checkSize, fileSize))
                                            .as("파일 끝 부분 내용 검증")
                                            .isEqualTo(Arrays.copyOfRange(content, fileSize - checkSize, fileSize));
                                });
                            }
                        }
                    }));
        }
        
        static Stream<Arguments> fileSizeProvider() {
            return Stream.of(
                Arguments.of(1, "매우 작은 파일 (1KB)"),
                Arguments.of(10, "작은 파일 (10KB)"),
                Arguments.of(100, "일반 파일 (100KB)"),
                Arguments.of(1024, "중간 크기 파일 (1MB)"),
                Arguments.of(5 * 1024, "대용량 파일 (5MB)")
            );
        }
    }

    @Nested
    @DisplayName("여러 파일 동시 저장 및 로드")
    class MultipleFilesTest {
        
        @Test
        @DisplayName("여러 파일 저장 및 로드")
        void shouldHandleMultipleFiles() {
            // Given: 여러 파일 준비
            int fileCount = 5;
            String[] filenames = new String[fileCount];
            String[] contents = new String[fileCount];
            String[] keys = new String[fileCount];
            
            // When: 다양한 파일들 저장
            for (int i = 0; i < fileCount; i++) {
                filenames[i] = generateUniqueFilename("-" + i + ".txt");
                contents[i] = "파일 " + i + "의 테스트 내용: " + UUID.randomUUID();
                
                FileResource fileResource = createMockStoredFile(
                        filenames[i], 
                        contents[i]
                );
                
                keys[i] = repository.store(fileResource);
            }
            
            // Then: 각 파일 저장 결과 확인
            for (int i = 0; i < fileCount; i++) {
                assertThat(keys[i])
                        .as("%d번째 파일의 키가 파일명과 일치해야 함", i)
                        .isEqualTo(filenames[i]);
            }
            
            // And When: 모든 파일 로드
            for (int i = 0; i < fileCount; i++) {
                final int index = i; // 람다에서 사용하기 위한 final 변수
                
                // Then: 각 파일 로드 결과 검증
                assertThat(repository.load(keys[i]))
                        .as("%d번째 파일이 성공적으로 로드되어야 함", i)
                        .isPresent()
                        .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                            try (InputStream is = resource.getInputStream()) {
                                String loadedContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                                assertThat(loadedContent)
                                        .as("%d번째 파일의 내용이 원본과 일치해야 함", index)
                                        .isEqualTo(contents[index]);
                            }
                        }));
            }
        }
    }
    
    @Nested
    @DisplayName("다양한 MIME 타입 처리")
    class MimeTypeTests {
        
        @ParameterizedTest(name = "{0} 파일 처리")
        @MethodSource("mimeTypeProvider")
        @DisplayName("다양한 MIME 타입 파일 저장 및 로드")
        void shouldHandleVariousMimeTypes(String description, String extension, String mimeType, byte[] sampleContent) {
            // Given: 특정 MIME 타입의 파일
            String filename = generateUniqueFilename(extension);
            FileResource fileResource = createMockStoredFile(
                    filename,
                    sampleContent,
                    mimeType
            );

            // When: 파일 저장
            String key = repository.store(fileResource);

            // Then: 저장 결과 확인
            assertThat(key).isEqualTo(filename);

            // And When: 파일 로드
            Optional<UrlResource> resourceOpt = repository.load(key);

            // Then: 로드 결과 검증
            assertThat(resourceOpt)
                    .as("%s 파일이 성공적으로 로드되어야 함", description)
                    .isPresent()
                    .hasValueSatisfying(resource -> assertThatNoException().isThrownBy(() -> {
                        try (InputStream is = resource.getInputStream()) {
                            byte[] loadedContent = is.readAllBytes();
                            assertThat(loadedContent)
                                    .as("%s 파일의 내용이 원본과 일치해야 함", description)
                                    .isEqualTo(sampleContent);
                        }
                    }));
        }
        
        static Stream<Arguments> mimeTypeProvider() {
            return Stream.of(
                Arguments.of(
                    "텍스트 파일", 
                    ".txt", 
                    "text/plain", 
                    "일반 텍스트 파일입니다.".getBytes(StandardCharsets.UTF_8)
                ),
                Arguments.of(
                    "HTML 파일", 
                    ".html", 
                    "text/html", 
                    "<html><body><h1>HTML 테스트</h1></body></html>".getBytes(StandardCharsets.UTF_8)
                ),
                Arguments.of(
                    "JSON 파일", 
                    ".json", 
                    "application/json", 
                    "{\"name\":\"테스트\",\"value\":123}".getBytes(StandardCharsets.UTF_8)
                ),
                Arguments.of(
                    "바이너리 파일", 
                    ".bin", 
                    "application/octet-stream", 
                    new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}
                ),
                Arguments.of(
                    "PDF 더미 파일", 
                    ".pdf", 
                    "application/pdf", 
                    "%PDF-1.5\nDummy PDF Content".getBytes(StandardCharsets.UTF_8)
                )
            );
        }
    }

    // 테스트 유틸리티 메서드

    private String generateUniqueFilename(String extension) {
        return "test-file-" + UUID.randomUUID() + extension;
    }

    private FileResource createMockStoredFile(String filename, String content) {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return createMockStoredFile(filename, contentBytes, "text/plain");
    }

    private FileResource createMockStoredFile(String filename, byte[] content, String contentType) {
        FileResource fileResource = mock(FileResource.class);
        given(fileResource.filename()).willReturn(filename);
        given(fileResource.content()).willReturn(new ByteArrayInputStream(content));
        given(fileResource.size()).willReturn((long) content.length);
        given(fileResource.contentType()).willReturn(contentType);
        return fileResource;
    }
}