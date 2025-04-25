package gettothepoint.unicatapi.filestorage.config;

import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableConfigurationProperties(LocalFileStorageProperties.class)
public class LocalTestConfig {
}