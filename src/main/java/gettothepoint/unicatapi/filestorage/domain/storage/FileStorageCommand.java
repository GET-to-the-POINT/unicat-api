package gettothepoint.unicatapi.filestorage.domain.storage;

import lombok.Builder;
import lombok.NonNull;
import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 파일 저장 요청 커맨드 객체.
 * 생성 시점에 모든 유효성 검증을 수행합니다.
 */
@Builder
public record FileStorageCommand(
        @NonNull String filename,
        @NonNull InputStream content,
        long size,
        @NonNull String contentType
) {
    private static final Tika tika = new Tika();
    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
            ".jpg",  "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png",  "image/png",
            ".gif",  "image/gif",
            ".txt",  "text/plain"
    );

    // 파일명 유효성 검사를 위한 정규식 패턴들
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|[\\\\/])\\.\\.($|[\\\\/])");
    private static final Pattern LEADING_DOT_PATTERN = Pattern.compile("^\\.(?![\\\\/])");
    private static final Pattern MULTIPLE_DOTS_PATTERN = Pattern.compile("\\.\\.");
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[:*?\"<>|]");

    /** IO 예외를 던질 수 있는 Consumer */
    @FunctionalInterface
    private interface IoConsumer<T> {
        void accept(T t) throws IOException;
    }

    /**
     * mark/reset 지원 스트림에 대해 공통적으로 mark → 작업 → reset 순서를 수행한다.
     */
    private static void withMarkReset(InputStream in, IoConsumer<InputStream> work) throws IOException {
        in.mark(Integer.MAX_VALUE);
        work.accept(in);
        in.reset();
    }

    public FileStorageCommand {
        validateFilename(filename);
        validatePositiveSize(size);

        try {
            if (!content.markSupported()) {
                throw new IllegalArgumentException("입력 스트림은 mark/reset을 지원해야 합니다");
            }

            withMarkReset(content, s -> validateContentSize(s, size));
            @NonNull String finalFilename = filename;
            withMarkReset(content, s -> validateFileTypeAndMime(s, finalFilename, contentType));
            AtomicReference<String> nameRef = new AtomicReference<>(filename);
            withMarkReset(content, s -> {
                String hashCode = generateSha256(s);
                nameRef.set(transformFilename(nameRef.get(), hashCode));
            });
            filename = nameRef.get();

        } catch (IOException e) {
            throw new IllegalArgumentException("파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 경로/확장자를 보존하면서 파일명에 해시코드를 삽입한다.
     *
     * 예)
     *  dir/dir2/text.txt   -> dir/dir2/text.<hash>.txt
     *  dir/dir2/text       -> dir/dir2/text.<hash>
     *  text.txt            -> text.<hash>.txt
     *  text                -> text.<hash>
     */
    private String transformFilename(String filename, String hashCode) {
        // 1) 마지막 경로 구분자 찾기 (윈도우, 유닉스 모두 지원)
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

        String dirPart  = lastSlash >= 0 ? filename.substring(0, lastSlash + 1) : "";      // "dir/dir2/"
        String namePart = lastSlash >= 0 ? filename.substring(lastSlash + 1) : filename;    // "text.txt"

        // 2) 마지막 점(.) 위치 찾기
        int lastDot = namePart.lastIndexOf('.');

        // 점이 없거나 이름이 ".bashrc" 처럼 맨 앞이면 확장자 없음으로 간주
        boolean hasExt = lastDot > 0 && lastDot < namePart.length() - 1;

        String baseName = hasExt ? namePart.substring(0, lastDot) : namePart;   // "text"
        String ext      = hasExt ? namePart.substring(lastDot)      : "";       // ".txt" or ""

        // 3) 새 파일명 생성: baseName.<hashCode>[.ext]
        return dirPart + baseName + "." + hashCode + ext;
    }

    /** 입력 스트림 전체를 읽어 SHA‑256 해시를 계산하고 문자열을 반환 */
    private String generateSha256(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다", e);
        }
    }

    /** 파일 크기 검증: 0보다 커야 함 */
    private static void validatePositiveSize(long size) {
        if (size <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다");
        }
    }

    /** 파일명 종합 검증 (빈 값 / 경로 조작 / OS 규칙) */
    private static void validateFilename(String filename) {
        if (filename.isBlank()) {
            throw new IllegalArgumentException("파일명은 빈 값일 수 없습니다");
        }

        // 경로 조작 패턴 체크
        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            throw new IllegalArgumentException("경로 조작이 감지되었습니다: " + filename);
        }
        // 절대 경로 패턴 체크 (리눅스/유닉스/윈도우)
        if (filename.startsWith("/") || filename.startsWith("\\") || filename.matches("^[a-zA-Z]:[\\\\/].*")) {
            throw new IllegalArgumentException("경로 조작이 감지되었습니다: " + filename);
        }
        // 맨 앞 점(.)으로 시작하는 파일명 (숨김파일)
        if (LEADING_DOT_PATTERN.matcher(filename).find()) {
            throw new IllegalArgumentException("파일명에 금지된 패턴이 포함되어 있습니다: " + filename);
        }
        // ..이 포함된 파일명 (ex: folder..test.txt, ..malicious.txt)
        if (MULTIPLE_DOTS_PATTERN.matcher(filename).find()) {
            throw new IllegalArgumentException("파일명에 금지된 패턴이 포함되어 있습니다: " + filename);
        }
        // 금지된 문자 포함
        if (FORBIDDEN_CHARS_PATTERN.matcher(filename).find()) {
            throw new IllegalArgumentException("파일명에 금지된 문자가 포함되어 있습니다: " + filename);
        }

        // OS별 금지된 문자 체크
        String os = System.getProperty("os.name").toLowerCase();

        // Windows 특수 규칙 체크
        if (os.contains("win") && (filename.endsWith(".") || filename.endsWith(" "))) {
            throw new IllegalArgumentException("Windows에서는 파일명이 마침표나 공백으로 끝날 수 없습니다");
        }
    }

    /** 실제 InputStream 크기와 제공된 size 값이 일치하는지 확인 */
    private static void validateContentSize(InputStream content, long expectedSize) throws IOException {
        long actualSize = content.available();
        if (actualSize != expectedSize) {
            throw new IllegalArgumentException(
                    String.format("제공된 크기(%d)와 실제 컨텐츠 크기(%d)가 일치하지 않습니다",
                            expectedSize, actualSize)
            );
        }
    }

    /** 확장자 허용 여부, 감지된 MIME, 제공된 MIME 세 항목을 비교해 검증 */
    private static void validateFileTypeAndMime(InputStream content, String filename, String providedContentType)
            throws IOException {
        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.')).toLowerCase()
                : "";

        String expectedMimeType = ALLOWED_MIME_TYPES.get(ext);
        if (expectedMimeType == null) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다: " + ext);
        }

        String detectedMimeType = tika.detect(content, filename)
                .split(";")[0].trim();

        // 파일 확장자와 감지된 컨텐츠 타입 비교
        if (!expectedMimeType.equals(detectedMimeType)) {
            throw new IllegalArgumentException(
                    String.format("파일 확장자와 컨텐츠 타입 불일치: 확장자 '%s'는 '%s' 타입이어야 하나 '%s' 감지됨",
                            ext, expectedMimeType, detectedMimeType)
            );
        }

        // 제공된 컨텐츠 타입과 감지된 타입 비교
        if (!providedContentType.equals(detectedMimeType)) {
            throw new IllegalArgumentException(
                    String.format("컨텐츠 타입 불일치: 제공된 '%s'와 감지된 '%s'가 다릅니다",
                            providedContentType, detectedMimeType)
            );
        }
    }
}
