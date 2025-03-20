package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static File getOrCreateTemp(String fileHash) {
        return getOrCreateTemp(fileHash, ".tmp");
    }

    public static File getOrCreateTemp(String fileHash, String extension) {
        String tempDir = System.getProperty("java.io.tmpdir");
        return new File(tempDir, "unicat_cache_" + fileHash + extension);
    }
}
