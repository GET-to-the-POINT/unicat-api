package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new RuntimeException("임시 파일 생성 중 오류 발생", e);
        }
    }

    public static File getFile(String filename) {
        String path = System.getProperty("java.io.tmpdir");
        return new File(path, filename);
    }

    public static File getFilePath(String filename) {
        return new File(filename);
    }
}