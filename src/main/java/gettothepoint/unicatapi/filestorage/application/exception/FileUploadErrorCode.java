package gettothepoint.unicatapi.filestorage.application.exception;

import gettothepoint.unicatapi.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 업로드 관련 오류 코드를 정의하는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum FileUploadErrorCode implements ErrorCode {
    
    EMPTY_FILE("FS-U001", "업로드할 파일이 비어 있습니다."),
    INVALID_FILE("FS-U002", "업로드할 파일이 유효하지 않습니다."),
    INVALID_FILE_KEY("FS-U003", "파일 키가 유효하지 않습니다.");
    
    private final String code;
    private final String message;
}