package gettothepoint.unicatapi.filestorage.infrastructure.storage.config;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.local.LocalFileStorageRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocalFileStorageProperties.class)
public class LocalFileStorageConfig {

    @Bean
    public FileStorageRepository localFileStorageRepository(LocalFileStorageProperties props) {
        return new LocalFileStorageRepository(props.localRoot());
    }

}
