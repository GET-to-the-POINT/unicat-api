package gettothepoint.unicatapi.filestorage.infrastructure.config;

import gettothepoint.unicatapi.filestorage.domain.storage.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommandValidator;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.DefaultFileStorageCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DefaultFileStorageCommandConfig {

    private final FileStorageCommandValidator fileStorageCommandValidator;
    private final FileNameTransformer fileNameTransformer;

    @PostConstruct
    public void init() {
        DefaultFileStorageCommand.configure(fileStorageCommandValidator, fileNameTransformer);
    }

}