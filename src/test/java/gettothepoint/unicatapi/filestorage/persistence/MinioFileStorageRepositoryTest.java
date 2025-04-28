package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.FileResource;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.UrlResource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@DisplayName("MinIO 파일 저장소 통합 테스트")
class MinioFileStorageRepositoryTest {

    @Container
    static final MinIOContainer MINIO = new MinIOContainer("minio/minio:latest");
    private static final String BUCKET = "test-bucket";
    private static MinioFileStorageRepository repository;

    @BeforeAll
    static void setUpRepository() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = MinioClient.builder().endpoint(MINIO.getS3URL()).credentials(MINIO.getUserName(), MINIO.getPassword()).build();

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }

        repository = new MinioFileStorageRepository(client, BUCKET);
    }

    @Test
    @DisplayName("파일을 저장하고 다시 불러올 수 있다")
    void shouldStoreAndLoadFileSuccessfully() {
        // given
        String filename = "mocked-file.txt";
        byte[] bytes = "Mocked Content".getBytes();
        InputStream contentStream = new ByteArrayInputStream(bytes);

        FileResource mockFileResource = mock(FileResource.class);
        when(mockFileResource.getFilename()).thenReturn(filename);
        when(mockFileResource.getContent()).thenReturn(contentStream);
        when(mockFileResource.getContentType()).thenReturn("text/plain");
        when(mockFileResource.getSize()).thenReturn((long) bytes.length);

        // when
        String storedKey = repository.store(mockFileResource);          // 업로드
        Optional<UrlResource> presigned = repository.load(storedKey); // presigned URL 조회

        // then
        assertThat(storedKey).as("store() 는 원본 파일명을 반환해야 한다").isEqualTo(filename);

        assertThat(presigned).as("load() 는 존재하는 파일에 대해 presigned UrlResource 를 반환해야 한다").isPresent().get().satisfies(urlResource -> assertThat(urlResource.getURL().toString()).contains(BUCKET).contains(filename));
    }

    @Test
    @DisplayName("존재하지 않는 키를 조회하면 빈 Optional 이 반환된다")
    void shouldReturnEmptyWhenFileNotExists() {
        // when
        Optional<UrlResource> result = repository.load("missing-file.txt");

        // then
        assertThat(result).as("없는 파일은 빈 Optional 이어야 한다").isEmpty();
    }
}