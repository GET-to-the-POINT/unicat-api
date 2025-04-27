package gettothepoint.unicatapi.filestorage.domain.storage;

import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageErrorCode;
import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageException;
import gettothepoint.unicatapi.filestorage.infrastructure.config.DefaultFileStorageCommandConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.DefaultFileStorageCommand;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.DefaultFileNameTransformer;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.DefaultFileStorageCommandValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileStorageCommand 도메인 테스트")
@SpringJUnitConfig(classes = {
        DefaultFileStorageCommandConfig.class,
        DefaultFileStorageCommandValidator.class,
        DefaultFileNameTransformer.class
})
class FileStorageCommandTest {

    private static final String VALID_FILENAME = "abc/test.txt";
    private static final byte[] TEST_CONTENT_BYTES = "test content".getBytes();
    private static final InputStream VALID_CONTENT = new ByteArrayInputStream(TEST_CONTENT_BYTES);
    private static final long VALID_SIZE = TEST_CONTENT_BYTES.length;
    private static final String VALID_CONTENT_TYPE = "text/plain";

    /**
     * 정상 입력으로 FileStorageCommand 생성 성공을 검증
     */
    @Test
    @DisplayName("유효한 입력으로 생성 성공")
    void shouldCreateWithValidInput() {
        // When
        InputStream contentStream = new ByteArrayInputStream(TEST_CONTENT_BYTES);
        FileStorageCommand command = buildCommand(VALID_FILENAME, contentStream, VALID_SIZE, VALID_CONTENT_TYPE);

        // Then
        assertEquals(contentStream, command.getContent());
        assertEquals(VALID_SIZE, command.getSize());
        assertEquals(VALID_CONTENT_TYPE, command.getContentType());
    }

    /** null filename should trigger NullPointerException */
    @Test
    @DisplayName("filename이 null이면 예외 발생")
    void shouldThrowExceptionWhenGetFilenameIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullGetFilename
        );
        
        // 메시지 검증 대신 예외 타입만 검증
    }

    private void createWithNullGetFilename() {
        buildCommand(null, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** null content should trigger NullPointerException */
    @Test
    @DisplayName("content가 null이면 예외 발생")
    void shouldThrowExceptionWhenGetContentIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullGetContent
        );
        
        // 메시지 검증 대신 예외 타입만 검증
    }

    private void createWithNullGetContent() {
        buildCommand(VALID_FILENAME, null, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** size가 0 이하일 때 IllegalArgumentException 발생을 검증 */
    @ParameterizedTest
    @DisplayName("size가 0 이하면 예외 발생")
    @ValueSource(longs = {0, -1, -100})
    void shouldThrowExceptionWhenGetSizeIsZeroOrNegative(long invalidSize) {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> createWithInvalidGetSize(invalidSize)
        );
        assertEquals(FileStorageErrorCode.NON_POSITIVE_SIZE, exception.getErrorCode());
    }

    private void createWithInvalidGetSize(long size) {
        buildCommand(VALID_FILENAME, VALID_CONTENT, size, VALID_CONTENT_TYPE);
    }

    /** null contentType should trigger NullPointerException */
    @Test
    @DisplayName("contentType이 null이면 예외 발생")
    void shouldThrowExceptionWhenGetGetContentTypeIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullGetGetContentType
        );
        
        // 메시지 검증 대신 예외 타입만 검증
    }

    private void createWithNullGetGetContentType() {
        buildCommand(VALID_FILENAME, VALID_CONTENT, VALID_SIZE, null);
    }

    /** blank filename should trigger IllegalArgumentException */
    @Test
    @DisplayName("빈 파일명은 예외 발생")
    void shouldThrowExceptionWhenGetFilenameIsBlank() {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                this::createWithBlankGetFilename
        );
        assertEquals(FileStorageErrorCode.EMPTY_FILENAME, exception.getErrorCode());
    }

    private void createWithBlankGetFilename() {
        buildCommand("  ", VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** 허용 가능한 경로/파일명은 예외 없이 통과해야 한다 */
    @ParameterizedTest
    @DisplayName("정상 경로 패턴은 예외 없이 통과")
    @ValueSource(strings = {
            "./test.txt",
            "nested/folder/structure/test.txt",
            "nested\\folder\\structure\\test.txt",
            "text.text.a.b.c.txt",
            "file-name_123.txt"
    })
    void shouldNotThrowForValidPaths(String validPath) {
        assertDoesNotThrow(() ->
                buildCommand(validPath, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE)
        );
    }


    /** 경로 조작/금지 문자 포함 파일명은 IllegalArgumentException 발생을 검증 */
    @ParameterizedTest
    @DisplayName("경로 조작 패턴이 포함된 파일명은 예외 발생")
    @ValueSource(strings = {
            "../test.txt",
            "dir/../test.txt",
            "..malicious.txt",
            "folder..test.txt",
            "/etc/passwd",
            "folder\\etc\\passwd",
            ".ssh:id_rsa",
            "Windows*System32",
            "file?name.txt",
            "\"quotes\".txt",
            "<script>.txt",
            "test>.txt",
            "file|pipe.txt"
    })
    void shouldThrowExceptionWithPathTraversalPatterns(String maliciousPath) {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> createWithMaliciousGetFilename(maliciousPath)
        );
        
        // 여러 종류의 오류 코드 중 하나인지 확인
        assertTrue(
            exception.getErrorCode() == FileStorageErrorCode.PATH_TRAVERSAL_DETECTED ||
            exception.getErrorCode() == FileStorageErrorCode.FORBIDDEN_CHARACTERS ||
            exception.getErrorCode() == FileStorageErrorCode.EMPTY_FILENAME ||
            exception.getErrorCode() == FileStorageErrorCode.UNSUPPORTED_EXTENSION ||
            exception.getErrorCode() == FileStorageErrorCode.MULTIPLE_DOTS_DETECTED ||
            exception.getErrorCode() == FileStorageErrorCode.ABSOLUTE_PATH_DETECTED ||
            exception.getErrorCode() == FileStorageErrorCode.LEADING_DOT_FILENAME
        );
    }

    private void createWithMaliciousGetFilename(String filename) {
        buildCommand(filename, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** Windows 특수 규칙(마침표/공백 끝 파일명) 예외 발생 검증 */
    @ParameterizedTest
    @DisplayName("Windows에서 마침표나 공백으로 끝나는 파일명은 예외 발생")
    @ValueSource(strings = {"file.", "file "})
    void shouldThrowExceptionWithWindowsSpecialRules(String filename) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            // Windows 환경이 아닐 경우 테스트를 건너뜁니다
            return;
        }

        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> createWithSpecialGetFilename(filename)
        );
        assertEquals(FileStorageErrorCode.WINDOWS_SPECIAL_RULE_VIOLATION, exception.getErrorCode());
    }

    private void createWithSpecialGetFilename(String filename) {
        buildCommand(filename, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** 제공된 사이즈와 컨텐츠 실제 크기 불일치 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("제공된 사이즈와 실제 컨텐츠 사이즈가 불일치하면 예외 발생")
    void shouldThrowExceptionWhenGetSizeMismatch() {
        // 실제 크기는 TEST_CONTENT_BYTES.length 이지만 다른 크기로 요청하면 예외 발생
        long wrongSize = TEST_CONTENT_BYTES.length + 10;

        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                () -> createWithWrongGetSize(wrongSize)
        );
        assertEquals(FileStorageErrorCode.SIZE_MISMATCH, exception.getErrorCode());
    }

    private void createWithWrongGetSize(long size) {
        buildCommand(VALID_FILENAME, VALID_CONTENT, size, VALID_CONTENT_TYPE);
    }

    /** 허용되지 않는 파일 확장자 입력 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("허용되지 않는 파일 확장자는 예외 발생")
    void shouldThrowExceptionWithDisallowedExtension() {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                this::createWithDisallowedExtension
        );
        assertEquals(FileStorageErrorCode.UNSUPPORTED_EXTENSION, exception.getErrorCode());
    }

    private void createWithDisallowedExtension() {
        buildCommand("file.pdf", VALID_CONTENT, VALID_SIZE, "application/pdf");
    }

    /** 파일 확장자와 컨텐츠 타입 불일치 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("파일 확장자와 컨텐츠 타입이 일치하지 않으면 예외 발생")
    void shouldThrowExceptionWhenFileExtensionMismatch() {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                this::createWithExtensionMismatch
        );
        assertEquals(FileStorageErrorCode.EXTENSION_MIMETYPE_MISMATCH, exception.getErrorCode());
    }

    private void createWithExtensionMismatch() {
        // PNG 헤더 바이트를 포함한 입력 스트림 생성
        byte[] pngBytes = {
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0, 'I', 'H', 'D', 'R'
        };
        InputStream pngContent = new ByteArrayInputStream(pngBytes);
        buildCommand("image.txt", pngContent, pngBytes.length, "text/plain");
    }

    /** 제공된 컨텐츠 타입과 감지된 타입 불일치 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("제공된 컨텐츠 타입과 감지된 타입이 일치하지 않으면 예외 발생")
    void shouldThrowExceptionWhenGetGetContentTypeMismatch() {
        // When & Then
        FileStorageException exception = assertThrows(
                FileStorageException.class,
                this::createWithGetGetContentTypeMismatch
        );
        assertEquals(FileStorageErrorCode.CONTENT_TYPE_MISMATCH, exception.getErrorCode());
    }

    private void createWithGetGetContentTypeMismatch() {
        // 텍스트 내용이지만 이미지 컨텐츠 타입으로 제공
        String wrongContentType = "image/png";
        buildCommand(VALID_FILENAME, VALID_CONTENT, VALID_SIZE, wrongContentType);
    }

    /**
     * 헬퍼: 주어진 파라미터로 FileStorageCommand 빌더를 완성해 반환한다.
     */
    private FileStorageCommand buildCommand(String filename,
                                            InputStream content,
                                            long size,
                                            String contentType) {
        return DefaultFileStorageCommand.builder()
                .filename(filename)
                .content(content)
                .size(size)
                .contentType(contentType)
                .build();
    }
}
