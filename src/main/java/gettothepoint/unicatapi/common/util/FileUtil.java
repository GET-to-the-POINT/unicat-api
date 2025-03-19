package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    public static void deleteLocalFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.delete()) {
            log.info("로컬 파일 삭제 완료: " + file.getAbsolutePath());
        } else {
            log.warn("로컬 파일 삭제 실패: " + file.getAbsolutePath());
        }
    }

}
