package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Primary
public class LocalStorageService extends AbstractStorageService {

    @Override
    protected File realDownload(String fileUrl) {
        File cacheFile = FileUtil.getFilePath(fileUrl);
        if (!cacheFile.exists()) {
            throw new RuntimeException("File not found for hash: " + fileUrl);
        }

        return cacheFile;
    }

    @Override
    public String upload(MultipartFile file) {
        String tempPath = System.getProperty("java.io.tmpdir");
        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File tmpFile = File.createTempFile(tempPath, extension);
            file.transferTo(tmpFile);
            return tmpFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public String upload(File file) {
        File foundFile = FileUtil.getFile(file.getName());
        if (foundFile.exists()) {
            return foundFile.getAbsolutePath();
        }

        String tempPath = System.getProperty("java.io.tmpdir");
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        File tmpFile = FileUtil.createTempFile(tempPath, extension);
        return tmpFile.getAbsolutePath();
    }
}
