package gettothepoint.unicatapi.filestorage.domain;

import lombok.Getter;
import lombok.NonNull;
import org.apache.tika.Tika;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 파일 리소스 관리를 위한 불변 객체
 * <p>
 * 이 클래스는 파일 업로드 처리, 보안 검증, 메타데이터 추출 등을 담당합니다.
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>파일 내용과 메타데이터 캡슐화</li>
 *   <li>파일명 보안 검증</li>
 *   <li>MIME 타입 감지 및 확장자 검증</li>
 *   <li>저장을 위한 해시 기반 파일명 생성</li>
 * </ul>
 */
@Getter
public final class FileResource {

    // ===== 상수 정의 =====

    /**
     * MIME 타입 감지를 위한 Tika 인스턴스
     */
    private static final Tika TIKA = new Tika();

    /**
     * 허용된 파일 확장자와 MIME 타입 매핑
     */
    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(".jpg", "image/jpeg", ".jpeg", "image/jpeg", ".png", "image/png", ".gif", "image/gif", ".txt", "text/plain");

    /**
     * 파일명 검증을 위한 정규표현식 패턴들
     */
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|[\\\\/])\\.\\.($|[\\\\/])");
    private static final Pattern LEADING_DOT_PATTERN = Pattern.compile("^\\.(?![\\\\/])");
    private static final Pattern MULTIPLE_DOTS_PATTERN = Pattern.compile("\\.\\.");
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[:*?\"<>|]");

    // ===== 인스턴스 필드 =====

    /**
     * 해시 적용 후 저장될 파일명
     */
    private final String filename;

    /**
     * 파일 내용 (바이트 배열)
     */
    private final byte[] data;

    /**
     * 파일 크기 (바이트)
     */
    private final long size;

    /**
     * MIME 타입 (Tika로 판별)
     */
    private final String contentType;

    // ===== 생성자 =====

    /**
     * 파일명과 바이트 배열로 파일 리소스를 생성합니다.
     *
     * @param filename 원본 파일명
     * @param content          파일 내용 바이트 배열
     * @throws NullPointerException     매개변수가 null인 경우
     * @throws IllegalArgumentException 파일이 비어있거나 파일명이 유효하지 않은 경우
     * @throws SecurityException        보안 위험이 있는 파일명인 경우
     */
    public FileResource(@NonNull String filename, byte @NonNull [] content) {
        validateFileName(filename);

        this.data = content;
        this.size = data.length;
        if (size <= 0) throw new IllegalArgumentException("파일이 비어 있습니다");

        this.contentType = detectMimeType(filename, data);
        validateExtensionAgainstMime(filename, contentType);

        this.filename = transformFilename(filename, data);
    }

    /**
     * 파일명의 유효성을 검증합니다.
     *
     * @throws IllegalArgumentException 파일명이 유효하지 않은 경우
     * @throws SecurityException        보안상 위험한 파일명인 경우
     */
    private static void validateFileName(String name) {
        if (!StringUtils.hasText(name)) throw new IllegalArgumentException("파일명은 비어 있을 수 없습니다");

        if (PATH_TRAVERSAL_PATTERN.matcher(name).find()) throw new SecurityException("경로 조작 감지: " + name);

        if (name.startsWith("/") || name.startsWith("\\") || name.matches("^[a-zA-Z]:[\\\\/].*"))
            throw new IllegalArgumentException("절대 경로 금지: " + name);

        if (LEADING_DOT_PATTERN.matcher(name).find())
            throw new IllegalArgumentException("파일명은 . 로 시작할 수 없습니다: " + name);

        if (MULTIPLE_DOTS_PATTERN.matcher(name).find()) throw new SecurityException("의심스러운 .. 포함: " + name);

        if (FORBIDDEN_CHARS_PATTERN.matcher(name).find()) throw new IllegalArgumentException("금지 문자가 포함된 파일명: " + name);

        if (System.getProperty("os.name").toLowerCase().contains("win") && (name.endsWith(".") || name.endsWith(" ")))
            throw new IllegalArgumentException("Windows에서 파일명은 . 또는 공백으로 끝날 수 없습니다: " + name);
    }

    /**
     * 파일 내용과 이름을 기반으로 MIME 타입을 감지합니다.
     */
    private static String detectMimeType(String name, byte[] data) {
        String mime = TIKA.detect(data, name);
        return mime.split(";")[0].trim(); // charset 제거
    }

    /**
     * 파일 확장자와 감지된 MIME 타입을 비교/검증합니다.
     *
     * @throws IllegalArgumentException 허용되지 않는 확장자이거나 확장자와 MIME 타입이 불일치하는 경우
     */
    private static void validateExtensionAgainstMime(String name, String mime) {
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')).toLowerCase() : "";
        String expected = ALLOWED_MIME_TYPES.get(ext);

        if (expected == null) throw new IllegalArgumentException("허용되지 않는 확장자: " + ext);

        if (!expected.equals(mime)) throw new IllegalArgumentException("확장자와 MIME 불일치: " + ext + " → " + mime);
    }

    /**
     * 원본 파일명을 해시 기반 파일명으로 변환합니다.
     */
    private static String transformFilename(String original, byte[] data) {
        int lastSlash = Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\'));
        String dir = lastSlash >= 0 ? original.substring(0, lastSlash + 1) : "";
        String name = lastSlash >= 0 ? original.substring(lastSlash + 1) : original;

        int lastDot = name.lastIndexOf('.');
        boolean hasExt = lastDot > 0 && lastDot < name.length() - 1;
        String base = hasExt ? name.substring(0, lastDot) : name;
        String ext = hasExt ? name.substring(lastDot) : "";

        String hash = sha256(data);
        return dir + base + "." + hash + ext;
    }

    /**
     * 바이트 배열의 SHA-256 해시를 계산합니다.
     */
    private static String sha256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 지원 불가", e);
        }
    }

    /**
     * MultipartFile과 디렉터리로 파일 리소스를 생성합니다.
     *
     * @param file      업로드된 MultipartFile
     * @param directory 저장될 디렉터리 경로
     * @throws NullPointerException     매개변수가 null인 경우
     * @throws IllegalArgumentException 파일이 비어있거나 유효하지 않은 경우
     * @throws UncheckedIOException     파일 읽기 중 오류 발생 시
     */
    public FileResource(@NonNull MultipartFile file, @NonNull Path directory) {
        byte[] content;
        try {
            if (file.isEmpty() || file.getSize() == 0) {
                throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다");
            }
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
        this(directory.resolve(file.getOriginalFilename()).toString().replace(File.separator, "/"), content);
    }

    /**
     * File 객체와 디렉터리로 파일 리소스를 생성합니다.
     *
     * @param file      업로드할 File 객체
     * @param directory 저장될 디렉터리 경로
     * @throws NullPointerException     매개변수가 null인 경우
     * @throws IllegalArgumentException 파일이 비어있거나 유효하지 않은 경우
     * @throws UncheckedIOException     파일 읽기 중 오류 발생 시
     */
    public FileResource(@NonNull File file, @NonNull Path directory) {
        byte[] content;
        try {
            if (!file.exists() || !file.canRead() || file.length() == 0) {
                throw new IllegalArgumentException("유효하지 않은 파일입니다");
            }
            content = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
        this(directory.resolve(file.getName()).toString().replace(File.separator, "/"), content);
    }

    /**
     * 파일 내용을 InputStream으로 반환합니다.
     *
     * @return 파일 내용이 담긴 InputStream
     */
    public InputStream getContent() {
        return new ByteArrayInputStream(data);
    }
}
