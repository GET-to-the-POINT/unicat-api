package gettothepoint.unicatapi.filestorage.config;

import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.MinioFileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableConfigurationProperties(MinioFileStorageProperties.class)
public class MinioTestConfig {
}