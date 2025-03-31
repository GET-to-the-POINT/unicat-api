package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.common.util.FileUtil;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository("supabaseS3FileStorageRepository")
@RequiredArgsConstructor
public class SupabaseFileStorageRepository implements FileStorageRepository {

    private final SupabaseProperties supabaseProperties;
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(supabaseProperties.s3().region()))
                .endpointOverride(URI.create(supabaseProperties.s3().endpoint()))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(supabaseProperties.s3().accessKeyId(), supabaseProperties.s3().secretAccessKey())
                ))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .checksumValidationEnabled(false)
                                .build()
                )
                .build();
    }

    public String save(File file) {
        // 파일명 추출
        String key = file.getName();

        // 확장자(컨텐츠 타입 결정)
        int dotIndex = key.lastIndexOf('.');
        String contentType = (dotIndex != -1) ? key.substring(dotIndex) : "";

        // 파일 타입에 따른 버킷 결정
        String bucket = getBucketName(contentType);

        // PutObjectRequest 생성 (public-read ACL 적용)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl("public-read")
                .build();

        // 파일 업로드 (파일의 경로로부터 RequestBody 생성)
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file.toPath()));

        // 공개 URL 생성
        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");
        return String.format("%s/object/%s/%s", publicEndpoint, bucket, key);
    }

    public String save(MultipartFile file) {
        // Obtain the original file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }

        // Extract key from the original filename
        int slashIndex = originalFilename.lastIndexOf('/');
        String key = (slashIndex != -1) ? originalFilename.substring(slashIndex + 1) : originalFilename;

        // Determine file extension for content type
        int dotIndex = key.lastIndexOf('.');
        String contentType = (dotIndex != -1) ? key.substring(dotIndex) : "";

        // Get bucket name based on content type
        String bucket = getBucketName(contentType);

        // Build the PutObjectRequest with public-read ACL
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl("public-read")
                .build();

        // Upload the file using the input stream from the MultipartFile
        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        // Construct the public URL
        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");
        return String.format("%s/object/%s/%s", publicEndpoint, bucket, key);
    }

    @Override
    public String save(String filepath) {

        Path path = Paths.get(filepath);
        String key = path.getFileName().toString();

        int dotIndex = key.lastIndexOf('.');
        String contentType = (dotIndex != -1) ? key.substring(dotIndex) : "";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(getBucketName(contentType))
                .key(key)
                .acl("public-read")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));

        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");

        URI uri = null;
        try {
            uri = new URI(String.format("%s/object/%s/%s",
                    publicEndpoint,
                    getBucketName(contentType),
                    key));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("올바르지 않은 URL 형식입니다: " + uri);
        }

        return uri.toASCIIString();
    }

    public File getFile(String fileUrl) {
        URI uri = URI.create(fileUrl);
        String path = uri.getPath();
        String prefix = "/storage/v1/object/";
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("올바르지 않은 URL 형식입니다: " + fileUrl);
        }

        String remainder = path.substring(prefix.length());
        int slashIndex = remainder.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException("URL에서 객체 키를 찾을 수 없습니다: " + fileUrl);
        }

        String bucket = remainder.substring(0, slashIndex);
        String key = remainder.substring(slashIndex + 1);

        String tempDir = FileUtil.getTempPath().toString();
        String localFilePath = Paths.get(tempDir, key).toString();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        Path localPath = Paths.get(localFilePath);

        // 부모 디렉토리가 없으면 생성
        try {
            Files.createDirectories(localPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("로컬 디렉토리 생성 실패: " + localPath.getParent(), e);
        }

        s3Client.getObject(getObjectRequest, localPath);
        return new File(localFilePath);
    }

    public String getBucketName(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return supabaseProperties.storage().bucket();
        }
        return switch (contentType) {
            case ".png", ".jpg", ".jpeg", ".webp" -> "image";
            case ".mp3", ".wav" -> "voice";
            case ".mp4", ".avi" -> "video";
            default -> supabaseProperties.storage().bucket();
        };
    }

    public ListObjectsV2Response getFolderListInBucket(String bucketName, String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix + "/")
                .build();

        return s3Client.listObjectsV2(request);
    }
}
