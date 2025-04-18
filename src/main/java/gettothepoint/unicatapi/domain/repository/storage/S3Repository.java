package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.properties.S3Properties;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("s3Repository")
@RequiredArgsConstructor
public class S3Repository implements FileRepository {

    private final S3Properties s3Properties;
    private final S3Client s3Client;

    @Override
    public Optional<File> findFileByKey(Path keyPath) {
        String bucket = keyPath.getName(0).toString();
        String bucketKey = keyPath.subpath(1, keyPath.getNameCount()).toString();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(bucketKey).build();

        Path absolutePath = FileUtil.getAbsolutePath(keyPath);
        try {
            File file = absolutePath.toFile();
            if (file.exists()) {
                return Optional.of(file);
            }

            s3Client.getObject(getObjectRequest, absolutePath);
            return Optional.of(absolutePath.toFile());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("S3 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public Optional<URI> findUriByKey(Path keyPath) {
        String bucket = keyPath.getName(0).toString();
        String bucketKey = keyPath.subpath(1, keyPath.getNameCount()).toString();

        try {
            S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(s3Properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Properties.accessKeyId(), s3Properties.secretAccessKey())))
                .endpointOverride(URI.create(s3Properties.endpoint()))
                .build();

            String contentType = FileUtil.guessContentTypeFromKey(keyPath);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(
                            GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(bucketKey)
                                    .responseContentDisposition("inline")
                                    .responseContentType(contentType)
                                    .build()
                    )
                    .build();

            URI presignedUrl = presigner.presignGetObject(presignRequest).url().toURI();
            presigner.close();
            return Optional.of(presignedUrl);
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("S3 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("S3 프리사인드 URL 생성 중 오류 발생", e);
        }
    }

    @Override
    public Path save(MultipartFile file) {
        Path absoluteHashedPath = FileUtil.getAbsoluteHashedPath(file);
        Path keyPath = FileUtil.getRelativePath(absoluteHashedPath);
        PutObjectRequest putObjectRequest = getObjectRequest(keyPath);

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        return keyPath;
    }

    @Override
    public Path save(File file) {
        Path absoluteHashedPath = FileUtil.getAbsoluteHashedPath(file);
        Path keyPath = FileUtil.getRelativePath(absoluteHashedPath);
        PutObjectRequest putObjectRequest = getObjectRequest(keyPath);
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return keyPath;
    }

    private PutObjectRequest getObjectRequest(Path relativePath) {
        String bucket = relativePath.getName(0).toString();
        String bucketKey = relativePath.subpath(1, relativePath.getNameCount()).toString();

        return PutObjectRequest.builder().bucket(bucket).key(bucketKey).acl("public-read").build();
    }

    public List<AssetItem> assets() {
        String bucket = s3Properties.bucket();

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        return assets(listRequest);
    }

    public List<AssetItem> assets(String directory) {
        String bucket = s3Properties.bucket();
        String prefix = directory.endsWith("/") ? directory : directory + "/";

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        return assets(listRequest);
    }

    private List<AssetItem> assets(ListObjectsV2Request listObjectsV2Request) {
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listObjectsV2Request);
        List<AssetItem> assets = new ArrayList<>();

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(s3Properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Properties.accessKeyId(), s3Properties.secretAccessKey())))
                .endpointOverride(URI.create(s3Properties.endpoint()))
                .build();

        for (S3Object s3Object : listResponse.contents()) {
            String bucketKey = s3Object.key();
            Path bucketPathKey = Path.of(bucketKey);
            String name = bucketPathKey.getFileName().toString();

            String contentType = FileUtil.guessContentTypeFromKey(bucketPathKey);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(s3Properties.bucket())
                            .key(bucketKey)
                            .responseContentDisposition("inline")
                            .responseContentType(contentType)
                            .build())
                    .build();

            URI presignedUrl;
            try {
                presignedUrl = presigner.presignGetObject(presignRequest).url().toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException("프리사인 URL 생성 실패", e);
            }
            assets.add(new AssetItem(name, s3Properties.bucket() + "/" + bucketKey, presignedUrl.toString()));
        }

        presigner.close();
        return assets;
    }

}