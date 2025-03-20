package gettothepoint.unicatapi.application.service.media;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.util.List;

public class MediaValidationUtil {

    private MediaValidationUtil() {}

    public static void validateImageFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image file path cannot be null or empty");
        }
        File imageFile = new File(filePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image file does not exist or is not a valid file: " + filePath);
        }
        String lowerImage = filePath.toLowerCase();
        if (!(lowerImage.endsWith(".jpg") || lowerImage.endsWith(".jpeg")
                || lowerImage.endsWith(".png") || lowerImage.endsWith(".bmp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 이미지 파일 형식입니다: " + filePath);
        }
    }

    public static void validateAudioFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Audio file path cannot be null or empty");
        }
        File audioFile = new File(filePath);
        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Audio file does not exist or is not a valid file: " + filePath);
        }
        String lowerAudio = filePath.toLowerCase();
        if (!(lowerAudio.endsWith(".mp3") || lowerAudio.endsWith(".aac")
                || lowerAudio.endsWith(".wav") || lowerAudio.endsWith(".flac"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 오디오 파일 형식입니다: " + filePath);
        }
    }

    public static void validateVideosFile(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file paths cannot be null or empty");
        }
        for (String filePath : filePaths) {
            validateVideoFile(filePath);
        }
    }

    private static void validateVideoFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file path cannot be null or empty");
        }
        File videoFile = new File(filePath);
        if (!videoFile.exists() || !videoFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file does not exist or is not a valid file: " + filePath);
        }
        String lowerVideo = filePath.toLowerCase();
        if (!(lowerVideo.endsWith(".mp4") || lowerVideo.endsWith(".mov") || lowerVideo.endsWith(".mkv") || lowerVideo.endsWith(".avi") || lowerVideo.endsWith(".flv"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원되지 않는 비디오 파일 형식입니다: " + filePath);
        }
    }
}
