package gettothepoint.unicatapi.filestorage.infrastructure.persistence;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.infrastructure.exception.MinioFileStorageException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static io.minio.http.Method.GET;

@RequiredArgsConstructor
public class MinioFileStorageRepository implements FileStorageRepository {

    private final MinioClient minioClient;
    private final String bucket;

    @Override
    public String store(FileResource file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.filename())
                            .stream(file.content(), file.size(), -1)
                            .contentType(file.contentType())
                            .build()
            );
            return file.filename();
        } catch (MinioException e) {
            throw MinioFileStorageException.uploadError(file.filename(), e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw MinioFileStorageException.authenticationError(e);
        } catch (IOException e) {
            throw MinioFileStorageException.uploadError(file.filename(), e);
        }
    }

    @Override
    public Optional<UrlResource> load(@NotNull String key) {
        try {
            // 존재 여부 확인
            minioClient.statObject(
                    StatObjectArgs.builder()
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
        } catch (MinioException e) {
            // 존재하지 않거나 접근 권한이 없는 경우
            return Optional.empty();
        } catch (MalformedURLException e) {
            // URL 형식이 올바르지 않은 경우 (매우 드문 경우)
            // logger.warn("잘못된 URL 형식: {}", key, e);
            return Optional.empty();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            // 인증 오류 (구성 문제)
            throw MinioFileStorageException.authenticationError(e);
        } catch (IOException e) {
            // I/O 오류는 보통 네트워크 문제
            // logger.warn("Minio 서버 연결 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }
}