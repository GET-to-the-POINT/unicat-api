package gettothepoint.unicatapi.filestorage.domain.storage;

import lombok.Builder;

import java.io.InputStream;

/**
 * 파일 저장 요청 커맨드 객체.
 * 생성 시점에 모든 유효성 검증을 수행합니다.
 */
@Builder
public record FileStorageCommand(String filename, InputStream content, long size, String contentType) {

    private static FileStorageCommandValidator validator;
    private static FileNameTransformer transformer;

    public static void configure(FileStorageCommandValidator v, FileNameTransformer t) {
        validator = v;
        transformer = t;
    }

    public FileStorageCommand(String filename, InputStream content, long size, String contentType) {
        if (validator == null || transformer == null) {
            throw new IllegalStateException("FileStorageCommandValidator와 FileNameTransformer가 설정되지 않았습니다.");
        }
        validator.validate(filename, content, size, contentType);

        this.filename = transformer.transform(filename, content);
        this.content = content;
        this.size = size;
        this.contentType = contentType;
    }
}