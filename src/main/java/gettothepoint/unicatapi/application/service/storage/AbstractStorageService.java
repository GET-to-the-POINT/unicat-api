package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.List;

@Log4j2
public abstract class AbstractStorageService implements StorageService {

    @Override
    public List<File> downloads(List<String> fileUrls) {
        return fileUrls.stream()
                .map(this::download)
                .toList();
    }

    @Override
    public File download(String fileUrl) {
        return ensureCacheFile(fileUrl);
    }

    private File ensureCacheFile(String url) {
        log.info("캐시 파일 확인: {}", url);
        File cacheFile = FileUtil.getTemp(url);

        if (cacheFile.exists()) {
            log.info("캐시 파일이 존재합니다: {}", cacheFile.getAbsolutePath());
            return cacheFile;
        }

        log.info("캐시 파일이 존재하지 않습니다. 다운로드를 시작합니다: {}", url);
        return realDownload(url);
    }

    protected abstract File realDownload(String fileUrl);
}