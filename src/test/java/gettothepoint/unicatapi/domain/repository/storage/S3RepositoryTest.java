package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.properties.S3Properties;
import gettothepoint.unicatapi.infrastructure.config.S3ClientConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {S3ClientConfig.class, S3Repository.class, S3RepositoryTest.TestConfig.class})
@Testcontainers
class S3RepositoryTest {

    @Container
    static MinIOContainer container =
            new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z");

    @Autowired
    S3Repository s3Repository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public S3Properties s3Properties() {
            String endpoint = container.getS3URL();
            return new S3Properties("unicat", endpoint, "us-east-1", container.getUserName(), container.getPassword());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "samples/image/sample.png",
            "samples/audio/sample01.mp3",
            "samples/transition/transition01.mp3"
    })
    void saveAndFindFileTest(String classpathResource) {
        ClassLoader classLoader = getClass().getClassLoader();
        var resourceUrl = classLoader.getResource(classpathResource);
        assertNotNull(resourceUrl, "리소스가 클래스패스에서 발견되지 않았습니다: " + classpathResource);

        File originalFile = new File(resourceUrl.getFile());
        assertTrue(originalFile.exists(), "리소스 파일이 존재해야 합니다");

        // 파일을 S3에 저장합니다. (S3Repository.save 메서드는 파일을 S3에 업로드하고, 저장된 파일의 경로를 반환합니다.)
        Path savedPath = s3Repository.save(originalFile);
        assertNotNull(savedPath, "저장된 경로가 null이 아닙니다");

        // 저장된 파일을 S3에서 찾아 검증합니다.
        Optional<File> foundFile = s3Repository.findFileByRelativePath(savedPath);
        assertTrue(foundFile.isPresent(), "저장된 파일을 S3에서 찾을 수 있어야 합니다");
        assertTrue(foundFile.get().exists(), "찾은 파일이 실제로 존재해야 합니다");
    }

    // Test method to be added in S3RepositoryTest.java
    @ParameterizedTest
    @ValueSource(strings = {
            "samples/image/sample.png",
            "samples/audio/sample01.mp3"
    })
    void assetsListTest(String classpathResource) {
        ClassLoader classLoader = getClass().getClassLoader();
        var resourceUrl = classLoader.getResource(classpathResource);
        assertNotNull(resourceUrl, "리소스가 클래스패스에서 발견되지 않았습니다: " + classpathResource);

        File originalFile = new File(resourceUrl.getFile());
        assertTrue(originalFile.exists(), "리소스 파일이 존재해야 합니다");

        Path savedPath = s3Repository.save(originalFile);
        assertNotNull(savedPath, "저장된 경로가 null이 아닙니다");

        var assets = s3Repository.assets();

        assertNotNull(assets, "assets 결과는 null이 아니어야 합니다");
        assertTrue(assets.stream().anyMatch(asset -> asset.key().endsWith(savedPath.getFileName().toString())),
                "저장된 파일이 assets 목록에 존재해야 합니다");
    }

}