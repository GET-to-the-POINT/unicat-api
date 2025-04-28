package gettothepoint.unicatapi.filestorage.infrastructure.exception;

/**
 * Minio 파일 스토리지 관련 예외
 */
public class MinioFileStorageException extends FileStorageInfraException {
    
    public MinioFileStorageException(FileStorageInfraErrorCode errorCode) {
        super(errorCode);
    }
    
    public MinioFileStorageException(FileStorageInfraErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
    
    public MinioFileStorageException(FileStorageInfraErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public MinioFileStorageException(FileStorageInfraErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
    
    /**
     * Minio 연결 오류 예외를 생성합니다.
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static MinioFileStorageException connectionError(Throwable cause) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_CONNECTION_ERROR, cause);
    }
    
    /**
     * Minio 인증 오류 예외를 생성합니다.
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static MinioFileStorageException authenticationError(Throwable cause) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_AUTHENTICATION_ERROR, cause);
    }
    
    /**
     * 버킷을 찾을 수 없음 예외를 생성합니다.
     * @param bucketName 버킷 이름
     * @return 생성된 예외
     */
    public static MinioFileStorageException bucketNotFound(String bucketName) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_BUCKET_NOT_FOUND, bucketName);
    }
    
    /**
     * 객체를 찾을 수 없음 예외를 생성합니다.
     * @param objectName 객체 이름
     * @return 생성된 예외
     */
    public static MinioFileStorageException objectNotFound(String objectName) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_OBJECT_NOT_FOUND, objectName);
    }
    
    /**
     * 객체 업로드 오류 예외를 생성합니다.
     * @param objectName 객체 이름
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static MinioFileStorageException uploadError(String objectName, Throwable cause) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_UPLOAD_ERROR, cause, objectName);
    }
    
    /**
     * 객체 다운로드 오류 예외를 생성합니다.
     * @param objectName 객체 이름
     * @param cause 원인 예외
     * @return 생성된 예외
     */
    public static MinioFileStorageException downloadError(String objectName, Throwable cause) {
        return new MinioFileStorageException(FileStorageInfraErrorCode.MINIO_DOWNLOAD_ERROR, cause, objectName);
    }
}