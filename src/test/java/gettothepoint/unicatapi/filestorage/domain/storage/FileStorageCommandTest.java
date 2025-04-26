package gettothepoint.unicatapi.filestorage.domain.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileStorageCommand 도메인 테스트")
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
        assertEquals(contentStream, command.content());
        assertEquals(VALID_SIZE, command.size());
        assertEquals(VALID_CONTENT_TYPE, command.contentType());
    }

    /** null filename should trigger NullPointerException */
    @Test
    @DisplayName("filename이 null이면 예외 발생")
    void shouldThrowExceptionWhenFilenameIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullFilename
        );
        assertTrue(exception.getMessage().contains("filename is marked non-null but is null"));
    }

    private void createWithNullFilename() {
        buildCommand(null, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** null content should trigger NullPointerException */
    @Test
    @DisplayName("content가 null이면 예외 발생")
    void shouldThrowExceptionWhenContentIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullContent
        );
        assertTrue(exception.getMessage().contains("content is marked non-null but is null"));
    }

    private void createWithNullContent() {
        buildCommand(VALID_FILENAME, null, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** size가 0 이하일 때 IllegalArgumentException 발생을 검증 */
    @ParameterizedTest
    @DisplayName("size가 0 이하면 예외 발생")
    @ValueSource(longs = {0, -1, -100})
    void shouldThrowExceptionWhenSizeIsZeroOrNegative(long invalidSize) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWithInvalidSize(invalidSize)
        );
        assertEquals("파일 크기는 0보다 커야 합니다", exception.getMessage());
    }

    private void createWithInvalidSize(long size) {
        buildCommand(VALID_FILENAME, VALID_CONTENT, size, VALID_CONTENT_TYPE);
    }

    /** null contentType should trigger NullPointerException */
    @Test
    @DisplayName("contentType이 null이면 예외 발생")
    void shouldThrowExceptionWhenContentTypeIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                this::createWithNullContentType
        );
        assertTrue(exception.getMessage().contains("contentType is marked non-null but is null"));
    }

    private void createWithNullContentType() {
        buildCommand(VALID_FILENAME, VALID_CONTENT, VALID_SIZE, null);
    }

    /** blank filename should trigger IllegalArgumentException */
    @Test
    @DisplayName("빈 파일명은 예외 발생")
    void shouldThrowExceptionWhenFilenameIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::createWithBlankFilename
        );
        assertEquals("파일명은 빈 값일 수 없습니다", exception.getMessage());
    }

    private void createWithBlankFilename() {
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
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWithMaliciousFilename(maliciousPath)
        );
        assertTrue(
                exception.getMessage().contains("경로 조작이 감지되었습니다")
                        || exception.getMessage().contains("파일명에 금지된 문자가 포함되어 있습니다")
                        || exception.getMessage().contains("파일명은 빈 값일 수 없습니다")
                        || exception.getMessage().contains("허용되지 않는 파일 확장자입니다")
                        || exception.getMessage().contains("파일명에 금지된 패턴이 포함되어 있습니다"),
                "Unexpected exception message: " + exception.getMessage()
        );
    }

    private void createWithMaliciousFilename(String filename) {
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
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWithSpecialFilename(filename)
        );
        assertEquals("Windows에서는 파일명이 마침표나 공백으로 끝날 수 없습니다", exception.getMessage());
    }

    private void createWithSpecialFilename(String filename) {
        buildCommand(filename, VALID_CONTENT, VALID_SIZE, VALID_CONTENT_TYPE);
    }

    /** 제공된 사이즈와 컨텐츠 실제 크기 불일치 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("제공된 사이즈와 실제 컨텐츠 사이즈가 불일치하면 예외 발생")
    void shouldThrowExceptionWhenSizeMismatch() {
        // 실제 크기는 TEST_CONTENT_BYTES.length 이지만 다른 크기로 요청하면 예외 발생
        long wrongSize = TEST_CONTENT_BYTES.length + 10;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createWithWrongSize(wrongSize)
        );
        assertTrue(exception.getMessage().contains("제공된 크기"));
        assertTrue(exception.getMessage().contains("실제 컨텐츠 크기"));
    }

    private void createWithWrongSize(long size) {
        buildCommand(VALID_FILENAME, VALID_CONTENT, size, VALID_CONTENT_TYPE);
    }

    /** 허용되지 않는 파일 확장자 입력 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("허용되지 않는 파일 확장자는 예외 발생")
    void shouldThrowExceptionWithDisallowedExtension() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::createWithDisallowedExtension
        );
        assertEquals("허용되지 않는 파일 확장자입니다: .pdf", exception.getMessage());
    }

    private void createWithDisallowedExtension() {
        buildCommand("file.pdf", VALID_CONTENT, VALID_SIZE, "application/pdf");
    }

    /** 파일 확장자와 컨텐츠 타입 불일치 시 IllegalArgumentException 발생 검증 */
    @Test
    @DisplayName("파일 확장자와 컨텐츠 타입이 일치하지 않으면 예외 발생")
    void shouldThrowExceptionWhenFileExtensionMismatch() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::createWithExtensionMismatch
        );
        assertTrue(exception.getMessage().contains("파일 확장자와 컨텐츠 타입 불일치"));
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
    void shouldThrowExceptionWhenContentTypeMismatch() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                this::createWithContentTypeMismatch
        );
        assertTrue(exception.getMessage().contains("컨텐츠 타입 불일치"));
    }

    private void createWithContentTypeMismatch() {
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
        return FileStorageCommand.builder()
                .filename(filename)
                .content(content)
                .size(size)
                .contentType(contentType)
                .build();
    }
}
