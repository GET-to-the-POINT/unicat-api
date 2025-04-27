package gettothepoint.unicatapi.filestorage.domain.policy;

import java.io.InputStream;

/**
 * 파일 저장 전 파일명을 변환하는 인터페이스입니다.
 * 다양한 변환 전략(해시 기반, UUID 기반 등)을 구현할 수 있습니다.
 */
public interface FileNameTransformer {
    /**
     * 파일명과 콘텐츠를 기반으로 새로운 파일명을 생성합니다.
     *
     * @param filename 원본 파일명
     * @param content 파일 내용 스트림 (mark/reset 지원 필요)
     * @return 변환된 파일명
     */
    String transform(String filename, InputStream content);
}