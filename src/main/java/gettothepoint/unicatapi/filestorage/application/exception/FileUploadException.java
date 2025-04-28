package gettothepoint.unicatapi.filestorage.application.exception;

import gettothepoint.unicatapi.common.exception.BaseException;

/**
 * 파일 업로드 과정에서 발생하는 예외
 */
public class FileUploadException extends BaseException {

    public FileUploadException(FileUploadErrorCode errorCode) {
        super(errorCode);
    }

    public FileUploadException(FileUploadErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public FileUploadException(FileUploadErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public FileUploadException(FileUploadErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}