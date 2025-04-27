package gettothepoint.unicatapi.filestorage.application.exception;

import lombok.Getter;

/**
 * 파일 다운로드 과정에서 발생하는 예외
 */
@Getter
public class FileDownloadException extends RuntimeException {

    private final FileDownloadErrorCode errorCode;

    public FileDownloadException(FileDownloadErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
    }
}