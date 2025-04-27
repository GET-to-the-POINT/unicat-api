package gettothepoint.unicatapi.filestorage.application.exception;

import lombok.Getter;

/**
 * 파일 업로드 관련 오류 코드를 정의하는 열거형
 */
@Getter
public enum FileUploadErrorCode {
    
    EMPTY_FILE("업로드할 파일이 비어 있습니다."),
    INVALID_FILE("업로드할 파일이 유효하지 않습니다."),
    INVALID_FILE_KEY("파일 키가 유효하지 않습니다.");
    
    private final String message;
    
    FileUploadErrorCode(String message) {
        this.message = message;
    }
    
    /**
     * 오류 메시지 포맷팅
     * 
     * @param args 포맷팅에 사용할 인자들
     * @return 포맷팅된 오류 메시지
     */
    public String formatMessage(Object... args) {
        if (args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }
}