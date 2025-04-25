package gettothepoint.unicatapi.filestorage.infrastructure.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.filestorage")
public record LocalFileStorageProperties(Path localRoot) {}