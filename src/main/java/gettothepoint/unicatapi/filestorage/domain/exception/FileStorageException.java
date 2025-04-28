package gettothepoint.unicatapi.filestorage.domain.exception;

import gettothepoint.unicatapi.common.exception.BaseException;
import gettothepoint.unicatapi.common.exception.ErrorCode;

/**
 * 파일 스토리지 도메인에서 발생하는 예외 
 */
public class FileStorageException extends BaseException {
    
    public FileStorageException(FileStorageErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public FileStorageException(FileStorageErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
