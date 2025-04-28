package gettothepoint.unicatapi.filestorage.infrastructure.exception;

import java.util.List;

/**
 * 복합 파일 스토리지 관련 예외
 */
public class CompositeFileStorageException extends FileStorageInfraException {
    
    private final List<Throwable> causes;
    
    public CompositeFileStorageException(FileStorageInfraErrorCode errorCode) {
        super(errorCode);
        this.causes = List.of();
    }
    
    public CompositeFileStorageException(FileStorageInfraErrorCode errorCode, List<Throwable> causes) {
        super(errorCode);
        this.causes = causes;
    }
    
    /**
     * 이 예외를 발생시킨 모든 원인 예외들을 반환합니다.
     * @return 원인 예외 목록
     */
    public List<Throwable> getAllCauses() {
        return causes;
    }
    
    /**
     * 대리자가 없는 경우 예외를 생성합니다.
     * @return 생성된 예외
     */
    public static CompositeFileStorageException noDelegates() {
        return new CompositeFileStorageException(FileStorageInfraErrorCode.COMPOSITE_NO_DELEGATES);
    }
    
    /**
     * 일부 저장소에 저장 실패 예외를 생성합니다.
     * @param causes 실패 원인 예외 목록
     * @return 생성된 예외
     */
    public static CompositeFileStorageException partialFailure(List<Throwable> causes) {
        return new CompositeFileStorageException(FileStorageInfraErrorCode.COMPOSITE_PARTIAL_FAILURE, causes);
    }
    
    /**
     * 모든 저장소에 저장 실패 예외를 생성합니다.
     * @param causes 실패 원인 예외 목록
     * @return 생성된 예외
     */
    public static CompositeFileStorageException completeFailure(List<Throwable> causes) {
        return new CompositeFileStorageException(FileStorageInfraErrorCode.COMPOSITE_COMPLETE_FAILURE, causes);
    }
}