package gettothepoint.unicatapi.filestorage.infrastructure.config;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.persistence.composite.CompositeFileStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class CompositeFileStorageConfig {

    @Bean(name = "compositeFileStorageRepository")
    @Primary
    public FileStorageRepository compositeFileStorageRepository(
            @Qualifier("localFileStorageRepository") FileStorageRepository localFileStorageRepository,
            @Autowired(required = false) @Qualifier("minioFileStorageRepository") FileStorageRepository minioFileStorageRepository) {
        return new CompositeFileStorageRepository(
                minioFileStorageRepository != null
                        ? List.of(minioFileStorageRepository, localFileStorageRepository)
                        : List.of(localFileStorageRepository)
        );
    }

}