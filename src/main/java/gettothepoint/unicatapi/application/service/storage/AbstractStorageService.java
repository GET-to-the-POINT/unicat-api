package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;

import java.io.File;
import java.util.List;

public abstract class AbstractStorageService implements StorageService {

    @Override
    public List<File> downloads(List<String> filenames) {
        return filenames.stream()
                .map(this::download)
                .toList();
    }

    @Override
    public File download(String filename) {
        return ensureCacheFile(filename);
    }

    private File ensureCacheFile(String filename) {
        File cacheFile = FileUtil.getFilenameInTemp(filename);

        if (cacheFile.exists()) {
            return cacheFile;
        }

        return realDownload(filename);
    }

    protected abstract File realDownload(String fileHash);
}