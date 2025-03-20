package gettothepoint.unicatapi.application.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public abstract class AbstractStorageService implements StorageService {

    @Override
    public File downloadFile(Integer fileHash) {
        return ensureCacheFile(fileHash);
    }

    @Override
    public InputStream download(Integer fileHash) {
        try {
            return new FileInputStream(ensureCacheFile(fileHash));
        } catch (IOException e) {
            throw new RuntimeException("Error reading cached file for hash: " + fileHash, e);
        }
    }

    private File ensureCacheFile(Integer fileHash) {
        File cacheFile = getCacheFile(fileHash);
        if (!cacheFile.exists()) {
            InputStream downloadedStream = donwload(fileHash);
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

    private File getCacheFile(Integer fileHash) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String fileName = "unicat_cache_" + fileHash;
        return new File(tmpDir, fileName);
    }

    protected abstract InputStream donwload(Integer fileHash);
}