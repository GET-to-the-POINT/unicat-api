package gettothepoint.unicatapi.filestorage.config;

import gettothepoint.unicatapi.filestorage.persistence.CompositeFileStorageRepository;
import gettothepoint.unicatapi.filestorage.persistence.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.persistence.LocalFileStorageRepository;
import gettothepoint.unicatapi.filestorage.persistence.MinioFileStorageRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@EnableConfigurationProperties({LocalFileStorageProperties.class, MinioFileStorageProperties.class})
public class FileStorageConfig {

    /* ---------- Local ---------- */
    @Bean
    public FileStorageRepository localFileStorageRepository(LocalFileStorageProperties props) {
        return new LocalFileStorageRepository(props.root());
    }

    /* ---------- MinIO ---------- */
    @Bean
    @Profile("minio")
    public MinioClient minioClient(MinioFileStorageProperties props) {
        MinioClient client = MinioClient.builder().endpoint(props.endpoint()).credentials(props.accessKeyId(), props.secretAccessKey()).build();

        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.bucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 연결 또는 버킷 생성 실패", e);
        }
        return client;
    }

    @Bean
    @Profile("minio")
    public FileStorageRepository minioFileStorageRepository(MinioClient client, MinioFileStorageProperties props) {
        return new MinioFileStorageRepository(client, props.bucket());
    }

    /* ---------- Composite (Default) ---------- */
    @Bean
    @Primary
    public FileStorageRepository fileStorageRepository(@Qualifier("localFileStorageRepository") FileStorageRepository local, @Autowired(required = false) @Qualifier("minioFileStorageRepository") FileStorageRepository minio) {

        // MinIO 사용 시 → MinIO + Local 묶음, 아니면 Local 단독
        return (minio != null) ? new CompositeFileStorageRepository(List.of(minio, local)) : local;
    }
}