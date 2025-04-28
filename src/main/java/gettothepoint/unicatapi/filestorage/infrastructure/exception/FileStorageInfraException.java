package gettothepoint.unicatapi.filestorage.infrastructure.exception;

import lombok.Getter;

/**
 * 파일 스토리지 인프라스트럭처 계층에서 발생하는 모든 예외의 기본 클래스
 */
@Getter
public abstract class FileStorageInfraException extends RuntimeException {
    
    private final FileStorageInfraErrorCode errorCode;
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    protected FileStorageInfraException(FileStorageInfraErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}