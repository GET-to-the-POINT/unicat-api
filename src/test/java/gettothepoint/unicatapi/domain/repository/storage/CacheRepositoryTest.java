package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.propertie.S3Properties;
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
@ContextConfiguration(classes = {S3ClientConfig.class, LocalRepository.class, S3Repository.class, CacheRepository.class, CacheRepositoryTest.TestConfig.class})
@Testcontainers
class CacheRepositoryTest {

    @Container
    static MinIOContainer container =
            new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z");

    @Autowired
    CacheRepository cacheRepository;

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
        Path savedPath = cacheRepository.save(originalFile);
        assertNotNull(savedPath, "저장된 경로가 null이 아닙니다");

        // 저장된 파일을 S3에서 찾아 검증합니다.
        Optional<File> foundFile = cacheRepository.findFileByRelativePath(savedPath);
        assertTrue(foundFile.isPresent(), "저장된 파일을 S3에서 찾을 수 있어야 합니다");
        assertTrue(foundFile.get().exists(), "찾은 파일이 실제로 존재해야 합니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "samples/image/sample.png",
            "samples/audio/sample01.mp3",
            "samples/transition/transition01.mp3"
    })
    void saveToS3AndGetFromCacheTest(String classpathResource) {
        ClassLoader classLoader = getClass().getClassLoader();
        var resourceUrl = classLoader.getResource(classpathResource);
        assertNotNull(resourceUrl, "리소스가 클래스패스에서 발견되지 않았습니다: " + classpathResource);

        File originalFile = new File(resourceUrl.getFile());
        assertTrue(originalFile.exists(), "리소스 파일이 존재해야 합니다");

        // 먼저 S3Repository를 사용해 파일 업로드 (캐시 사용 X)
        Path s3SavedPath = s3Repository.save(originalFile);
        assertNotNull(s3SavedPath, "S3에 저장된 경로가 null이 아닙니다");

        // cacheRepository를 통해 동일 key로 파일을 조회 -> 캐시에서 잘 가져오는지 확인
        // 실제 구현체에 따라 캐시에 없으면 S3에서 가져와 캐싱하는 로직이 들어있다고 가정
        Optional<File> cachedFile = cacheRepository.findFileByRelativePath(s3SavedPath);
        assertTrue(cachedFile.isPresent(), "캐시에서 파일을 찾을 수 있어야 합니다");
        assertTrue(cachedFile.get().exists(), "찾은 파일이 실제로 존재해야 합니다");

        // 이후 다시 한 번 findFileByKey 호출하여, 두 번째 호출 시에도 문제 없이 캐시에서 가져오는지 확인
        Optional<File> cachedFileAgain = cacheRepository.findFileByRelativePath(s3SavedPath);
        assertTrue(cachedFileAgain.isPresent(), "두 번째 캐시 조회도 성공해야 합니다");
        assertTrue(cachedFileAgain.get().exists(), "두 번째 조회한 파일도 실제로 존재해야 합니다");
    }

}