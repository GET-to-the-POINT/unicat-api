package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.infrastructure.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.LocalFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        FileUploadUseCase.class,
        CompositeFileStorageConfig.class,
        LocalFileStorageConfig.class,
})
@DisplayName("파일 업로드 유스케이스 Local 환경 통합 테스트")
class FileUploadUseCaseLocalIntegrationTest extends FileUploadUseCaseTestBase {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }
}
