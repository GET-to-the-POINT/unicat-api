package gettothepoint.unicatapi.filestorage.domain.storage.config;

import gettothepoint.unicatapi.filestorage.domain.storage.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommandValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FileStorageCommandConfig {

    private final FileStorageCommandValidator fileStorageCommandValidator;
    private final FileNameTransformer fileNameTransformer;

    @PostConstruct
    public void init() {
        FileStorageCommand.configure(fileStorageCommandValidator, fileNameTransformer);
    }

}