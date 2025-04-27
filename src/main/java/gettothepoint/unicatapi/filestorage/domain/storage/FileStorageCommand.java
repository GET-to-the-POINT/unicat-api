package gettothepoint.unicatapi.filestorage.domain.storage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.InputStream;

/**
 * 파일 저장 요청 커맨드 객체.
 * 생성 시점에 모든 유효성 검증을 수행합니다.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Getter
public abstract class FileStorageCommand {

    @NonNull
    private final String filename;

    @NonNull
    private final InputStream content;

    private final long size;

    @NonNull
    private final String contentType;
}