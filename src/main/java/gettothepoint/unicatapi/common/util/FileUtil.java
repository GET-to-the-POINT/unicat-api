package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_BUCKET = "unicat";

    private static Path getTempPath() {
        return Paths.get(TEMP_DIR);
    }

    public static Path getRelativePath(Path absolutePath) {
        return getTempPath().relativize(absolutePath);
    }

    public static Path getAbsolutePath(Path relativePath) {
        return getTempPath().resolve(relativePath);
    }

    public static Path getUniqueFilePath(String extension) {
        if (extension.indexOf('.') != 0) {
            extension = "." + extension;
        }
        String fileName = UUID.randomUUID() + extension;
        return getTempPath().resolve(DEFAULT_BUCKET).resolve(fileName);
    }
}