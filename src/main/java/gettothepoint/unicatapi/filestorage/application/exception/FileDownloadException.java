package gettothepoint.unicatapi.filestorage.application.exception;

import gettothepoint.unicatapi.common.exception.BaseException;

/**
 * 파일 다운로드 과정에서 발생하는 예외
 */
public class FileDownloadException extends BaseException {

    public FileDownloadException(FileDownloadErrorCode errorCode) {
        super(errorCode);
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public FileDownloadException(FileDownloadErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}