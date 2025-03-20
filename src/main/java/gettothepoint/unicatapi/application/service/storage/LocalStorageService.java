package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.dto.storage.UploadResult;
import gettothepoint.unicatapi.common.util.FileUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
@Primary
public class LocalStorageService extends AbstractStorageService {

    @Override
    protected InputStream realDownload(String fileHash) {
        File cacheFile = FileUtil.getOrCreateTemp(fileHash);
        if (cacheFile.exists()) {
            try {
                return new FileInputStream(cacheFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found for hash: " + fileHash, e);
            }
        } else {
            throw new RuntimeException("File not found for hash: " + fileHash);
        }
    }

    @Override
    public UploadResult upload(MultipartFile file, String fileMimeType) {
        try {
            return doUpload(file.getInputStream(), fileMimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload MultipartFile", e);
        }
    }

    @Override
    public UploadResult upload(File file, String fileMimeType) {
        try (InputStream is = new FileInputStream(file)) {
            return doUpload(is, fileMimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload File", e);
        }
    }

    @Override
    public UploadResult upload(InputStream inputStream, String fileMimeType) {
        return doUpload(inputStream, fileMimeType);
    }

    private UploadResult doUpload(InputStream inputStream, String fileMimeType) {
        try (InputStream is = inputStream) {
            byte[] bytes = is.readAllBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            String fileHash = Arrays.hashCode(hashBytes) + "";
            File target = FileUtil.getOrCreateTemp(fileHash);
            if (!target.exists()) {
                Files.write(target.toPath(), bytes);
            }
            return new UploadResult(fileHash, target.getAbsolutePath(), fileMimeType);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error during file upload", e);
        }
    }
}
