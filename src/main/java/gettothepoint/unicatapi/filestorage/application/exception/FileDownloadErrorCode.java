package gettothepoint.unicatapi.filestorage.application.exception;

import lombok.Getter;

/**
 * 파일 다운로드 관련 오류 코드를 정의하는 열거형
 */
@Getter
public enum FileDownloadErrorCode {

    INVALID_FILE_KEY("파일 키가 유효하지 않습니다."),
    FILE_NOT_FOUND("요청한 파일을 찾을 수 없습니다: %s"),
    ACCESS_DENIED("파일에 접근할 권한이 없습니다: %s"),
    STORAGE_ERROR("파일 저장소 연결 오류가 발생했습니다: %s");

    private final String message;

    FileDownloadErrorCode(String message) {
        this.message = message;
    }

    public String formatMessage(Object... args) {
        if (args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }
}