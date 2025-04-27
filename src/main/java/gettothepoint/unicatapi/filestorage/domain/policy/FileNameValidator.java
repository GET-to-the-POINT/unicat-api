package gettothepoint.unicatapi.filestorage.domain.policy;

/**
 * 파일명의 유효성을 검증하는 인터페이스입니다.
 * 파일명 관련 다양한 보안 및 호환성 검증을 담당합니다.
 */
public interface FileNameValidator {
    /**
     * 파일명이 유효한지 검증합니다.
     *
     * @param filename 검증할 파일명
     * @throws RuntimeException 파일명이 유효하지 않을 경우 예외 발생
     */
    void validate(String filename);
}
