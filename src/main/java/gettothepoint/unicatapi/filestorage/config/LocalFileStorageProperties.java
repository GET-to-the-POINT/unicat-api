package gettothepoint.unicatapi.filestorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "filestorage.local")
public record LocalFileStorageProperties(Path root) {}