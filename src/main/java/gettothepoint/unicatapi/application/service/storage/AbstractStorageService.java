package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public abstract class AbstractStorageService implements StorageService {

    @Override
    public List<InputStream> downloads(List<Integer> fileHashes) {
        return fileHashes.stream()
                .map(this::download)
                .toList();
    }

    @Override
    public InputStream download(Integer fileHash) {
        try {
            return new FileInputStream(ensureCacheFile(fileHash));
        } catch (IOException e) {
            throw new RuntimeException("Error reading cached file for hash: " + fileHash, e);
        }
    }

    @Override
    public File downloadFile(Integer fileHash) {
        return ensureCacheFile(fileHash);
    }

    private File ensureCacheFile(Integer fileHash) {
        File cacheFile = FileUtil.getOrCreateTemp(fileHash);
        if (!cacheFile.exists()) {
            InputStream downloadedStream = realDownload(fileHash);
            if (downloadedStream == null) {
                throw new RuntimeException("Failed to download file with hash: " + fileHash);
            }
            try {
                Files.copy(downloadedStream, cacheFile.toPath());
                downloadedStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error caching downloaded file with hash: " + fileHash, e);
            }
        }
        return cacheFile;
    }

    protected abstract InputStream realDownload(Integer fileHash);
}