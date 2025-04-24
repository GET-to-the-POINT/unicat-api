package gettothepoint.unicatapi.filestorage.infrastructure.storage.minio;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static io.minio.http.Method.GET;

@RequiredArgsConstructor
public class MinioFileStorageRepository implements FileStorageRepository {

    private final MinioClient minioClient;
    private final String bucket;

    @Override
    public String store(FileStorageCommand c) {
        // 파일 이름에 ../ 같은 경로 조작이 포함되어 있는지 확인
        Path p = Path.of(c.filename()).normalize();
        if (!p.getFileName().toString().equals(c.filename())) {
            throw new IllegalArgumentException("잘못된 경로: " + c.filename());
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(c.filename())
                            .stream(c.content(), c.size(), -1)
                            .contentType(c.contentType())
                            .build()
            );
            return c.filename();
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("파일 저장 실패: " + c.filename(), e);
        }
    }

    @Override
    public Optional<UrlResource> load(String key) {
        try {
            // 존재 여부 확인
            minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );

            // presigned URL 생성
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .method(GET)
                            .build()
            );

            return Optional.of(new UrlResource(presignedUrl));
        } catch (Exception e) {
            // 존재하지 않거나 오류 발생 시
            return Optional.empty();
        }
    }
}
