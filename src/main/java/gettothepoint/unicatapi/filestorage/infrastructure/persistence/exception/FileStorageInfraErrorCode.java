package gettothepoint.unicatapi.filestorage.infrastructure.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 스토리지 인프라스트럭처에서 발생할 수 있는 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum FileStorageInfraErrorCode {

    // 공통 에러 코드
    UNKNOWN_ERROR("FSI-000", "알 수 없는 오류가 발생했습니다"),

    // 로컬 파일 스토리지 관련 에러 코드
    LOCAL_DIRECTORY_CREATION_FAILED("FSI-L001", "로컬 저장소 디렉토리 생성에 실패했습니다: %s"),
    LOCAL_FILE_NOT_FOUND("FSI-L002", "요청한 파일을 찾을 수 없습니다: %s"),
    LOCAL_FILE_IO_ERROR("FSI-L003", "파일 입출력 중 오류가 발생했습니다: %s"),
    LOCAL_FILE_PERMISSION_DENIED("FSI-L004", "파일 작업 권한이 없습니다: %s"),
    LOCAL_FILE_ALREADY_EXISTS("FSI-L005", "파일이 이미 존재합니다: %s"),

    // Minio 파일 스토리지 관련 에러 코드
    MINIO_CONNECTION_ERROR("FSI-M001", "Minio 서버 연결에 실패했습니다"),
    MINIO_AUTHENTICATION_ERROR("FSI-M002", "Minio 서버 인증에 실패했습니다"),
    MINIO_BUCKET_NOT_FOUND("FSI-M003", "요청한 버킷을 찾을 수 없습니다: %s"),
    MINIO_OBJECT_NOT_FOUND("FSI-M004", "요청한 객체를 찾을 수 없습니다: %s"),
    MINIO_UPLOAD_ERROR("FSI-M005", "객체 업로드 중 오류가 발생했습니다: %s"),
    MINIO_DOWNLOAD_ERROR("FSI-M006", "객체 다운로드 중 오류가 발생했습니다: %s"),

    // 복합 파일 스토리지 관련 에러 코드
    COMPOSITE_NO_DELEGATES("FSI-C001", "등록된 저장소 위임자가 없습니다"),
    COMPOSITE_PARTIAL_FAILURE("FSI-C002", "일부 저장소에 저장 실패했습니다"),
    COMPOSITE_COMPLETE_FAILURE("FSI-C003", "모든 저장소에 저장 실패했습니다");

    private final String code;
    private final String message;

    /**
     * 포맷팅된 메시지를 반환합니다.
     * @param args 메시지 포맷팅에 사용할 인자들
     * @return 포맷팅된 메시지
     */
    public String formatMessage(Object... args) {
        try {
            return String.format(message, args);
        } catch (Exception e) {
            return message;
        }
    }
}