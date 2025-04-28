package gettothepoint.unicatapi.filestorage.infrastructure.exception;

import gettothepoint.unicatapi.common.exception.BaseException;

/**
 * 파일 스토리지 인프라스트럭처 계층에서 발생하는 모든 예외의 기본 클래스
 */
public abstract class FileStorageInfraException extends BaseException {
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode) {
        super(errorCode);
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}