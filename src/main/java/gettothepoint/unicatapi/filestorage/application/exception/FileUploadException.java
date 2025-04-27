package gettothepoint.unicatapi.filestorage.application.exception;

import lombok.Getter;

/**
 * 파일 업로드 과정에서 발생하는 예외
 */
@Getter
public class FileUploadException extends RuntimeException {

    private final FileUploadErrorCode errorCode;

    public FileUploadException(FileUploadErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public FileUploadException(FileUploadErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }

    public FileUploadException(FileUploadErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public FileUploadException(FileUploadErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
    }
}