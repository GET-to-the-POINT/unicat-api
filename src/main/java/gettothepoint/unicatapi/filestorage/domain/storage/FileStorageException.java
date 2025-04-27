package gettothepoint.unicatapi.filestorage.domain.storage;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {
    
    private final FileStorageErrorCode errorCode;
    
    public FileStorageException(FileStorageErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }
    
    public FileStorageException(FileStorageErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
    }
}
