package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;

import java.io.File;
import java.util.List;

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
        File cacheFile = FileUtil.getTemp(url);

        if (cacheFile.exists()) {
            return cacheFile;
        }

        return realDownload(url);
    }

    protected abstract File realDownload(String fileUrl);
}