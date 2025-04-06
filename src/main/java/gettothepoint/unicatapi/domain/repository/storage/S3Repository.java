package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.propertie.S3Properties;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import jakarta.annotation.PostConstruct;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("s3Repository")
@RequiredArgsConstructor
public class S3Repository implements FileRepository {

    private final S3Properties s3Properties;
    private final S3Client s3Client;

    @PostConstruct
    public void init() {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Properties.bucket()).build());
        } catch (BucketAlreadyOwnedByYouException ignored) {
            // 이미 있으면 무시
        }
    }

    @Override
    public Optional<File> findFileByKey(Path relativePath) {
        String bucket = relativePath.getName(0).toString();
        String key = relativePath.subpath(1, relativePath.getNameCount()).toString();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

        Path path = FileUtil.getAbsolutePath(relativePath);
        try {
            File file = path.toFile();
            if (file.exists()) {
                return Optional.of(file);
            }

            Files.createDirectories(path.getParent());
            s3Client.getObject(getObjectRequest, path);
            return Optional.of(path.toFile());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException("S3 오류 발생: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<URI> findUriByKey(Path relativePath) {
        String bucket = relativePath.getName(0).toString();
        String key = relativePath.subpath(1, relativePath.getNameCount()).toString();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

        try {
            S3Presigner presigner = S3Presigner.builder().region(Region.of(s3Properties.region())).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3Properties.accessKeyId(), s3Properties.secretAccessKey()))).endpointOverride(URI.create(s3Properties.endpoint())).build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(java.time.Duration.ofHours(24)) // 만 하루
                    .getObjectRequest(getObjectRequest).build();

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
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }

        Path filePath = FileUtil.getUniqueFilePath(extension);
        String bucket = filePath.getName(0).toString();
        String key = filePath.subpath(1, filePath.getNameCount()).toString().replace("\\", "/");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key).acl("public-read").build();
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        return FileUtil.getRelativePath(filePath);
    }

    @Override
    public Path save(File file) {
        String bucket = s3Properties.bucket();
        String key = file.getName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key).acl("public-read").build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return Path.of(bucket, key);
    }

    public List<AssetItem> listAssets(String bucket) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        List<AssetItem> items = new ArrayList<>();
        for (S3Object s3Object : response.contents()) {
            String key = s3Object.key();
            // 파일명 추출: key의 마지막 '/' 이후 문자열
            String name = key.substring(key.lastIndexOf('/') + 1);
            // URL 구성 (예: https://{endpoint}/{bucket}/{key})
            String url = s3Properties.endpoint() + "/" + bucket + "/" + key;
            items.add(new AssetItem(name, key, url));
        }
        return items;
    }
}