package gettothepoint.unicatapi.application.service.media;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

public class MediaValidationUtil {

    private MediaValidationUtil() {}

    private static final List<String> IMAGE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".bmp");
    private static final List<String> VIDEO_EXTENSIONS = List.of(".mp4", ".mov", ".mkv", ".avi", ".flv");
    private static final List<String> AUDIO_EXTENSIONS = List.of(".mp3", ".aac", ".wav", ".flac");

    public static void validateImageFile(String filePath) {
        validateExistence(filePath);
        if (!hasValidExtension(filePath, IMAGE_EXTENSIONS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 이미지 파일 형식입니다: " + filePath);
        }
    }

    public static void validateVideoFile(String filePath) {
        validateExistence(filePath);
        if (!hasValidExtension(filePath, VIDEO_EXTENSIONS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 비디오 파일 형식입니다: " + filePath);
        }
    }

    public static void validateAudioFile(String filePath) {
        validateExistence(filePath);
        if (!hasValidExtension(filePath, AUDIO_EXTENSIONS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 오디오 파일 형식입니다: " + filePath);
        }
    }

    public static void validateImageOrVideoFile(String filePath) {
        validateExistence(filePath);
        if (!hasValidExtension(filePath, IMAGE_EXTENSIONS) && !hasValidExtension(filePath, VIDEO_EXTENSIONS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 이미지 또는 비디오 파일 형식입니다: " + filePath);
        }
    }

    public static void validateVideosFile(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "비디오 파일 리스트가 비어있습니다.");
        }
        for (String path : filePaths) {
            validateVideoFile(path);
        }
    }

    private static void validateExistence(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 경로가 비어있습니다.");
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일이 존재하지 않거나 유효하지 않습니다: " + filePath);
        }
    }

    private static boolean hasValidExtension(String fileName, List<String> extensions) {
        return extensions.stream().anyMatch(fileName.toLowerCase()::endsWith);
    }
}