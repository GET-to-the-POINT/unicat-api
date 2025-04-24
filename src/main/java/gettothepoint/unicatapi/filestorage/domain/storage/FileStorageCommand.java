package gettothepoint.unicatapi.filestorage.domain.storage;

import java.io.InputStream;

/**
 * @param filename    저장할 이름
 * @param content     파일 데이터
 * @param size        바이트 단위 크기
 * @param contentType MIME 타입
 */
public record FileStorageCommand(String filename, InputStream content, long size, String contentType) {
}
