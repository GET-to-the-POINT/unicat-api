package gettothepoint.unicatapi.filestorage.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@TestConfiguration
public class LocalTestConfig {

    private Path tempDir;

    @Bean
    @Primary
    public LocalFileStorageProperties localFileStorageProperties() throws IOException {
        this.tempDir = Files.createTempDirectory("local-storage-test");
        return new LocalFileStorageProperties(tempDir.toString());
    }

    @PreDestroy
    public void cleanupTempDir() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete temp file: " + path);
                        }
                    });
        }
    }
}