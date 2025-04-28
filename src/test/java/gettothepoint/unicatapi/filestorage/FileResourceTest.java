package gettothepoint.unicatapi.filestorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FileResource 클래스 테스트")
class FileResourceTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("생성자 성공 테스트")
    class ConstructorSuccessTests {

        @Test
        @DisplayName("String과 InputStream으로 FileResource 생성")
        void createFromStringAndInputStream() {
            // given
            String filename = "test.txt";
            byte[] content = "Hello, World!".getBytes();
            InputStream inputStream = new ByteArrayInputStream(content);

            // when
            FileResource resource = new FileResource(filename, inputStream);

            // then
            assertThat(resource.getOriginalFilename()).isEqualTo(filename);
            assertThat(resource.getContentType()).isEqualTo("text/plain");
            assertThat(resource.getSize()).isEqualTo(content.length);
            
            byte[] resourceData = readAllBytes(resource.getContent());
            assertThat(resourceData).isEqualTo(content);
            
            // filename이 해시값을 포함하는지 확인
            String hash = calculateSha256(content);
            assertThat(resource.getFilename()).contains(hash);
        }

        @Test
        @DisplayName("String과 byte[]로 FileResource 생성")
        void createFromStringAndByteArray() {
            // given
            String filename = "test.jpg";
            byte[] content = new byte[]{0x00, 0x01, 0x02, 0x03}; // 간단한 바이너리 데이터

            // when
            FileResource resource = new FileResource(filename, content);

            // then
            assertThat(resource.getOriginalFilename()).isEqualTo(filename);
            assertThat(resource.getContentType()).startsWith("image/");
            assertThat(resource.getSize()).isEqualTo(content.length);
            assertThat(readAllBytes(resource.getContent())).isEqualTo(content);
        }

        @Test
        @DisplayName("MultipartFile과 디렉토리로 FileResource 생성")
        void createFromMultipartFile() {
            // given
            String filename = "test.txt";
            byte[] content = "Hello, World!".getBytes();
            MultipartFile multipartFile = new MockMultipartFile(
                    "file", 
                    filename, 
                    "text/plain",
                    content
            );

            // when
            FileResource resource = new FileResource(multipartFile, Path.of("test-dir1/test-dir2"));

            // then
            assertThat(resource.getSize()).isEqualTo(content.length);
            assertThat(readAllBytes(resource.getContent())).isEqualTo(content);
        }

        @Test
        @DisplayName("File과 디렉토리로 FileResource 생성")
        void createFromFile() throws IOException {
            // given
            String filename = "test.txt";
            byte[] content = "File content".getBytes();

            Path filePath = tempDir.resolve(filename);
            Files.write(filePath, content);
            File file = filePath.toFile();

            // when
            FileResource resource = new FileResource(file, Path.of("test-dir1/test-dir2"));

            // then
            assertThat(resource.getSize()).isEqualTo(content.length);
            assertThat(readAllBytes(resource.getContent())).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("파일명 검증 테스트")
    class FileNameValidationTests {

        @Test
        @DisplayName("null 파일명은 NullPointerException 발생")
        void nullFilenameThrowsException() {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatNullPointerException()
                    .isThrownBy(() -> new FileResource(null, content))
                    .withMessage("originalFilename must not be null");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 파일명은 NullPointerException 발생")
        void nullFilenameThrowsException(String invalidName) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatNullPointerException()
                    .isThrownBy(() -> new FileResource(invalidName, content))
                    .withMessage("originalFilename must not be null");
        }

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("빈 또는 공백 파일명은 IllegalArgumentException 발생")
        void emptyOrBlankFilenameThrowsException(String invalidName) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(invalidName, content))
                    .withMessageContaining("파일명은 비어 있을 수 없습니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"../secret.txt", "folder/../secret.txt"})
        @DisplayName("경로 탐색 시도가 포함된 파일명은 보안 예외 발생")
        void pathTraversalFilenameThrowsException(String traversalName) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatExceptionOfType(SecurityException.class)
                    .isThrownBy(() -> new FileResource(traversalName, content))
                    .satisfies(e -> {
                        assertThat(e.getMessage()).contains("경로 조작 감지");
                        assertThat(e.getMessage()).contains(traversalName);
                    });
        }

        @ParameterizedTest
        @ValueSource(strings = {".hidden", ".gitignore"})
        @DisplayName("점으로 시작하는 파일명은 예외 발생")
        void leadingDotFilenameThrowsException(String hiddenName) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new FileResource(hiddenName, content))
                    .withMessageContaining("파일명은 . 로 시작할 수 없습니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/absolute/path.txt", "\\windows\\path.txt", "C:\\windows\\file.txt"})
        @DisplayName("절대 경로는 예외 발생")
        void absolutePathThrowsException(String absolutePath) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new FileResource(absolutePath, content))
                    .withMessageContaining("절대 경로 금지");
        }

        @ParameterizedTest
        @ValueSource(strings = {"file:name.txt", "file*name.txt", "file?name.txt", 
                "file\"name.txt", "file<name.txt", "file>name.txt", "file|name.txt"})
        @DisplayName("금지된 문자가 포함된 파일명은 예외 발생")
        void forbiddenCharsThrowsException(String invalidName) {
            // given
            byte[] content = "test".getBytes();

            // when & then
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new FileResource(invalidName, content))
                    .withMessageContaining("금지 문자가 포함된 파일명");
        }
    }

    @Nested
    @DisplayName("MIME 타입 검증 테스트")
    class MimeTypeValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"test.exe", "test.bat", "test.com", "test.iso", "test.zip"})
        @DisplayName("허용되지 않는 확장자는 예외 발생")
        void disallowedExtensionThrowsException(String invalidExt) {
            // given
            byte[] content = "test content".getBytes();

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(invalidExt, content))
                    .withMessageContaining("허용되지 않는 확장자");
        }

        @Test
        @DisplayName("확장자와 실제 파일 내용의 MIME 타입이 불일치하면 예외 발생")
        void mismatchedMimeTypeThrowsException() throws IOException {
            // given - JPG 확장자이지만 텍스트 내용의 파일
            String filename = "fake-image.jpg";
            byte[] content = "This is not an image file".getBytes();

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(filename, content))
                    .withMessageContaining("확장자와 MIME 불일치")
                    .withMessageContaining(".jpg");
        }
    }

    @Nested
    @DisplayName("파일 내용 검증 테스트")
    class FileContentValidationTests {

        @Test
        @DisplayName("내용이 없는 파일은 예외 발생")
        void emptyContentThrowsException() {
            // given
            String filename = "empty.txt";
            byte[] emptyContent = new byte[0];

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(filename, emptyContent))
                    .withMessageContaining("파일이 비어 있습니다");
        }

        @Test
        @DisplayName("null 내용은 NullPointerException 발생")
        void nullContentThrowsException() {
            // given
            String filename = "test.txt";
            byte[] nullContent = null;

            // when & then
            assertThatNullPointerException()
                    .isThrownBy(() -> new FileResource(filename, nullContent))
                    .withMessage("content must not be null");
        }

        @Test
        @DisplayName("빈 MultipartFile은 예외 발생")
        void emptyMultipartFileThrowsException() {
            // given
            MultipartFile emptyFile = new MockMultipartFile(
                    "file", 
                    "empty.txt", 
                    "text/plain", 
                    new byte[0]
            );

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(emptyFile, tempDir))
                    .withMessageContaining("빈 파일은 업로드할 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 File 객체는 예외 발생")
        void nonExistentFileThrowsException() {
            // given
            File nonExistentFile = tempDir.resolve("non-existent.txt").toFile();

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(nonExistentFile, tempDir))
                    .withMessageContaining("유효하지 않은 파일입니다");
        }

        @Test
        @DisplayName("읽을 수 없는 File 객체는 예외 발생")
        void unreadableFileThrowsException() throws IOException {
            // given
            Path filePath = tempDir.resolve("unreadable.txt");
            Files.write(filePath, "test".getBytes());
            File unreadableFile = filePath.toFile();
            assertThat(unreadableFile.setReadable(false)).isTrue(); // 읽기 권한 제거

            // when & then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new FileResource(unreadableFile, tempDir))
                    .withMessageContaining("유효하지 않은 파일입니다");
        }
    }

    @Nested
    @DisplayName("파일명 변환 테스트")
    class FilenameTransformationTests {

        @Test
        @DisplayName("동일한 내용의 파일은 동일한 해시값을 가진 파일명 생성")
        void sameContentGeneratesSameHash() {
            // given
            String filename1 = "test1.txt";
            String filename2 = "test2.txt";
            byte[] sameContent = "identical content".getBytes();

            // when
            FileResource resource1 = new FileResource(filename1, sameContent);
            FileResource resource2 = new FileResource(filename2, sameContent);

            // then
            String hash1 = resource1.getFilename().substring(
                    resource1.getFilename().indexOf('.') + 1, 
                    resource1.getFilename().lastIndexOf('.')
            );
            String hash2 = resource2.getFilename().substring(
                    resource2.getFilename().indexOf('.') + 1, 
                    resource2.getFilename().lastIndexOf('.')
            );

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("다른 내용의 파일은 다른 해시값을 가진 파일명 생성")
        void differentContentGeneratesDifferentHash() {
            // given
            String sameFilename = "test.txt";
            byte[] content1 = "content1".getBytes();
            byte[] content2 = "content2".getBytes();

            // when
            FileResource resource1 = new FileResource(sameFilename, content1);
            FileResource resource2 = new FileResource(sameFilename, content2);

            // then
            assertThat(resource1.getFilename()).isNotEqualTo(resource2.getFilename());
        }

        @Test
        @DisplayName("원본 파일명의 경로와 확장자는 유지됨")
        void pathAndExtensionArePreserved() {
            // given
            String filename = "folder/subfolder/test.txt";
            byte[] content = "test content".getBytes();

            // when
            FileResource resource = new FileResource(filename, content);

            // then
            assertThat(resource.getFilename()).startsWith("folder/subfolder/");
            assertThat(resource.getFilename()).endsWith(".txt");
        }
    }

    // 유틸리티 메서드
    private byte[] readAllBytes(InputStream is) {
        try {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("읽기 실패", e);
        }
    }

    private String calculateSha256(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("해시 계산 실패", e);
        }
    }
}
