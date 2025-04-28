package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.FileResource;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.UrlResource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CompositeFileStorageRepository")
@Testcontainers
class CompositeFileStorageRepositoryTest {

    private static final DockerImageName MINIO_IMAGE =
            DockerImageName.parse("minio/minio:latest")
                            .asCompatibleSubstituteFor("minio/minio");

    @Container
    static final MinIOContainer MINIO = new MinIOContainer(MINIO_IMAGE);

    private static final String BUCKET = "test-bucket";

    @TempDir
    Path tempDir;

    private CompositeFileStorageRepository repository;

    /* ---------- repository builders ---------- */

    private CompositeFileStorageRepository localOnly() {
        FileStorageRepository local = new LocalFileStorageRepository(tempDir.getFileName());
        return new CompositeFileStorageRepository(List.of(local));
    }

    private CompositeFileStorageRepository localAndS3()
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
                   NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
                   XmlParserException, InternalException {

        MinioClient client = MinioClient.builder()
                                        .endpoint(MINIO.getS3URL())
                                        .credentials(MINIO.getUserName(), MINIO.getPassword())
                                        .build();

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }

        FileStorageRepository s3    = new MinioFileStorageRepository(client, BUCKET);
        FileStorageRepository local = new LocalFileStorageRepository(tempDir.getFileName());

        return new CompositeFileStorageRepository(List.of(local, s3));
    }

    /* ---------- tests ---------- */

    @Nested
    @DisplayName("When only local storage is configured")
    class LocalOnly {

        @BeforeEach
        void init() {
            repository = localOnly();
        }

        @Test
        @DisplayName("store()와 load()가 정상 동작한다")
        void storeAndLoadSuccessfully() {
            // given
            String filename = "mocked-file.txt";
            byte[] bytes = "Mocked Content".getBytes();
            InputStream contentStream = new ByteArrayInputStream(bytes);

            FileResource mockFileResource = mock(FileResource.class);
            when(mockFileResource.getFilename()).thenReturn(filename);
            when(mockFileResource.getContent()).thenReturn(contentStream);

            // when
            String storedKey = repository.store(mockFileResource);
            Optional<UrlResource> loaded = repository.load(storedKey);

            // then
            assertThat(storedKey).isEqualTo(filename);
            assertThat(loaded).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 키를 조회하면 빈 Optional을 반환한다")
        void returnEmptyForMissingKey() {
            Optional<UrlResource> loaded = repository.load("missing.txt");
            assertThat(loaded).isEmpty();
        }
    }

    @Nested
    @DisplayName("When local + S3 storage are configured")
    class LocalAndS3 {

        @BeforeEach
        void init() throws Exception {
            repository = localAndS3();
        }

        @Test
        @DisplayName("store()와 load()가 정상 동작한다")
        void storeAndLoadSuccessfully() {
            // given
            String filename = "mocked-file.txt";
            byte[] bytes = "Mocked Content".getBytes();
            InputStream contentStream = new ByteArrayInputStream(bytes);

            FileResource mockFileResource = mock(FileResource.class);
            when(mockFileResource.getFilename()).thenReturn(filename);
            when(mockFileResource.getContent()).thenReturn(contentStream);

            // when
            String storedKey = repository.store(mockFileResource);
            Optional<UrlResource> loaded = repository.load(storedKey);

            // then
            assertThat(storedKey).isEqualTo(filename);
            assertThat(loaded).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 키를 조회하면 빈 Optional을 반환한다")
        void returnEmptyForMissingKey() {
            Optional<UrlResource> loaded = repository.load("missing.txt");
            assertThat(loaded).isEmpty();
        }
    }
}