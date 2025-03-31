package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static gettothepoint.unicatapi.common.util.FileUtil.getTempPath;

@Repository("s3Repository")
@RequiredArgsConstructor
public class S3Repository implements FileRepository {

    private final SupabaseProperties supabaseProperties;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(supabaseProperties.s3().region()))
                .endpointOverride(URI.create(supabaseProperties.s3().endpoint()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                supabaseProperties.s3().accessKeyId(),
                                supabaseProperties.s3().secretAccessKey())))
                .serviceConfiguration(S3Configuration.builder().checksumValidationEnabled(false).build())
                .build();
    }

    // 버킷 존재 여부 확인 후 없으면 생성
    private void ensureBucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        }
    }

    @Override
    public File findByKey(String relativePath) {
        Path path = Paths.get(relativePath);
        String bucket = path.getName(0).toString();
        String key = path.subpath(1, path.getNameCount()).toString().replace("\\", "/");

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        Path localFilePath = getTempPath().resolve(relativePath);
        try {
            Files.createDirectories(localFilePath.getParent());
            s3Client.getObject(getObjectRequest, localFilePath);
        } catch (IOException e) {
            throw new RuntimeException("파일 다운로드 실패", e);
        }
        return localFilePath.toFile();
    }

    @Override
    public String save(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }
        String bucket = "resources";

        // 저장 전 버킷 존재 여부 확인
        ensureBucketExists(bucket);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(originalFilename)
                .acl("public-read")
                .build();
        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
        return bucket + "/" + originalFilename;
    }

    @Override
    public String save(File file) {
        String fileName = file.getName();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }
        Path filePath = Paths.get(fileName);
        String bucket = filePath.getName(0).toString();
        String key = filePath.subpath(1, filePath.getNameCount()).toString().replace("\\", "/");

        // 저장 전 버킷 존재 여부 확인
        ensureBucketExists(bucket);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl("public-read")
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
        return fileName;
    }

    public List<AssetItem> listAssets(String bucket) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        List<AssetItem> items = new ArrayList<>();
        for (S3Object s3Object : response.contents()) {
            String key = s3Object.key();
            // 파일명 추출: key의 마지막 '/' 이후 문자열
            String name = key.substring(key.lastIndexOf('/') + 1);
            // URL 구성 (예: https://{endpoint}/{bucket}/{key})
            String url = supabaseProperties.s3().endpoint() + "/" + bucket + "/" + key;
            items.add(new AssetItem(name, key, url));
        }
        return items;
    }
}