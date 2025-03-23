package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static Path getTempPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "unicat_uploads");
    }

    public static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException("임시 파일 생성 중 오류 발생", e);
        }
    }

    public static File getFilenameInTemp(String url) {
        String filename = filenameFromUrl(url);
        String path = getTempPath().toString();
        return new File(path, filename);
    }

    public static File getTemp(String url) {
        return new File(url);
    }

    private static String filenameFromUrl(String url) {
        return Paths.get(URI.create(url).getPath()).getFileName().toString();
    }
}