package gettothepoint.unicatapi.filestorage.application.exception;

import gettothepoint.unicatapi.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 다운로드 관련 오류 코드를 정의하는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum FileDownloadErrorCode implements ErrorCode {
    
    FILE_NOT_FOUND("FS-D001", "요청한 파일을 찾을 수 없습니다: %s"),
    DOWNLOAD_ERROR("FS-D002", "파일 다운로드 중 오류가 발생했습니다: %s"),
    INVALID_DOWNLOAD_KEY("FS-D003", "다운로드 키가 유효하지 않습니다.");
    
    private final String code;
    private final String message;

    public String formatMessage(Object... args) {
        if (args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }
}