package gettothepoint.unicatapi.domain.repository;

import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MinioIntegrationTest {

    @Container
    static MinIOContainer container =
            new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z");

    MinioClient minioClient = MinioClient
            .builder()
            .endpoint(container.getS3URL())
            .credentials(container.getUserName(), container.getPassword())
            .build();

    @Container
    static MinIOContainer container2 =
            new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withUserName("myAccessKey")
            .withPassword("mySecretKey");

    MinioClient minioClient2 = MinioClient
            .builder()
            .endpoint(container2.getS3URL())
            .credentials(container2.getUserName(), container2.getPassword())
            .build();


    private static final String BUCKET_NAME = "test-bucket";

    @BeforeAll
    static void beforeAll() {
        // Testcontainers가 컨테이너를 띄우고,
        // 필요 시 추가 설정 로직을 넣을 수 있습니다.
    }

    @BeforeEach
    void setUp() throws Exception {
        // 버킷이 이미 있을 수 있으므로 예외 캐치 후 무시
        try {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );
        } catch (Exception e) {
            // e가 "BucketAlreadyOwnedByYou" 등이라면 무시
            // (이미 버킷이 존재해도 문제 없으므로)
        }
    }

    @Test
    @DisplayName("Minio 업로드/다운로드 테스트")
    void testUploadDownload() throws Exception {
        // given
        String objectName = "test-file.txt";
        String content = "Hello Minio Container!";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        // when (업로드)
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .stream(
                                // 업로드할 데이터 스트림
                                new java.io.ByteArrayInputStream(bytes),
                                bytes.length, // 길이
                                -1 // partSize(자동)
                        )
                        .contentType("text/plain")
                        .build()
        );

        // then (다운로드)
        var response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .build()
        );
        String downloaded = new String(response.readAllBytes(), StandardCharsets.UTF_8);
        response.close(); // 스트림 닫기

        // 검증
        assertEquals(content, downloaded);
    }
}