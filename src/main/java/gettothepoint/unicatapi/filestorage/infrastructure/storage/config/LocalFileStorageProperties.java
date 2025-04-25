package gettothepoint.unicatapi.filestorage.infrastructure.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.filestorage")
public record LocalFileStorageProperties(String localRoot) {}