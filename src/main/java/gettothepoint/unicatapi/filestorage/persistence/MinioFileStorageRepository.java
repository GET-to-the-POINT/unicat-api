package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.domain.FileResource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.UncheckedIOException;
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
                            .object(file.getFilename())
                            .stream(file.getContent(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return file.getFilename();
        } catch (MinioException e) {
            throw new IllegalStateException("파일 업로드 실패: " + file.getFilename(), e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("MinIO 인증 실패", e);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 업로드 I/O 오류: " + file.getFilename(), e);
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
            throw new IllegalStateException("MinIO 인증 실패", e);
        } catch (IOException e) {
            // I/O 오류는 보통 네트워크 문제
            // logger.warn("Minio 서버 연결 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }
}