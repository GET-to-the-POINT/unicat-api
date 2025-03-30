package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
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

    @Override
    public String saveFile(String filepath) {

        int slashIndex = filepath.lastIndexOf('/');
        String key;
        if (slashIndex != -1) {
            key = filepath.substring(slashIndex + 1);
        } else {
            key = filepath;
        }

        int dotIndex = filepath.lastIndexOf('.');
        String contentType;
        if (dotIndex != -1) {
            contentType = filepath.substring(dotIndex);
        } else {
            contentType = "";
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(getBucketName(contentType))
                .key(key)
                .acl("public-read")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filepath)));

        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");
        return String.format("%s/object/%s/%s",
                publicEndpoint,
                getBucketName(contentType),
                key);
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

        String tempDir = System.getProperty("java.io.tmpdir");
        String localFilePath = Paths.get(tempDir, key).toString();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.getObject(getObjectRequest, Paths.get(localFilePath));
        return new File(localFilePath);
    }

    public String getBucketName(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return supabaseProperties.storage().bucket();
        }
        return switch (contentType) {
            case ".png", ".jpg", ".jpeg" -> "image";
            case ".mp3", ".wav" -> "voice";
            case ".mp4", ".avi" -> "video";
            default -> supabaseProperties.storage().bucket();
        };
    }
}
