package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static Path getTempPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "unicat_uploads");
    }

    public static File createTempFile(String suffix) {
        return createTempFile(getTempPath(), suffix);
    }

    private static File createTempFile(Path prefix, String suffix) {
        try {
            return File.createTempFile(prefix.toString(), suffix);
        } catch (IOException e) {
            log.error("임시 파일 생성 중 오류 발생", e);
            throw new RuntimeException("임시 파일 생성 중 오류 발생", e);
        }
    }

    public static File getTemp(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        Path tempPath = getTempPath();
        return new File(tempPath.toString(), fileName);
    }
}