package gettothepoint.unicatapi.filestorage.infrastructure.exception;

import java.io.IOException;

/**
 * 로컬 파일 스토리지 관련 예외
 */
public class LocalFileStorageException extends FileStorageInfraException {
    
    public LocalFileStorageException(FileStorageInfraErrorCode errorCode) {
        super(errorCode);
    }
    
    public LocalFileStorageException(FileStorageInfraErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public LocalFileStorageException(FileStorageInfraErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public LocalFileStorageException(FileStorageInfraErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
    
    /**
     * 디렉토리 생성 실패 예외를 생성합니다.
     * @param path 생성 실패한 디렉토리 경로
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static LocalFileStorageException directoryCreationFailed(String path, IOException cause) {
        return new LocalFileStorageException(FileStorageInfraErrorCode.LOCAL_DIRECTORY_CREATION_FAILED, cause, path);
    }
    
    /**
     * 파일 입출력 오류 예외를 생성합니다.
     * @param path 파일 경로
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static LocalFileStorageException fileIOError(String path, IOException cause) {
        return new LocalFileStorageException(FileStorageInfraErrorCode.LOCAL_FILE_IO_ERROR, cause, path);
    }
    
    /**
     * 파일을 찾을 수 없음 예외를 생성합니다.
     * @param path 파일 경로
     * @return 생성된 예외
     */
    public static LocalFileStorageException fileNotFound(String path) {
        return new LocalFileStorageException(FileStorageInfraErrorCode.LOCAL_FILE_NOT_FOUND, path);
    }
    
    /**
     * 파일 권한 거부 예외를 생성합니다.
     * @param path 파일 경로
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static LocalFileStorageException permissionDenied(String path, IOException cause) {
        return new LocalFileStorageException(FileStorageInfraErrorCode.LOCAL_FILE_PERMISSION_DENIED, cause, path);
    }
}