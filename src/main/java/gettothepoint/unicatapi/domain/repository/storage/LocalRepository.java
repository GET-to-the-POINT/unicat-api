package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Component("localRepository")
@RequiredArgsConstructor
public class LocalRepository implements FileRepository {

    @Override
    public Optional<File> findFileByKey(Path keyPath) {
        Path absolutePath = FileUtil.getAbsolutePath(keyPath);
        File file = absolutePath.toFile();
        if (file.exists()) {
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<URI> findUriByKey(Path keyPath) {
        throw new UnsupportedOperationException("Local repository does not support URI retrieval");
    }

    public Path save(MultipartFile file) {
        Path absoluteHashedPath = FileUtil.getAbsoluteHashedPath(file);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, absoluteHashedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        return FileUtil.getRelativePath(absoluteHashedPath);
    }

    @Override
    public Path save(File file) {
        Path absoluteHashedPath = FileUtil.getAbsoluteHashedPath(file);
        if (absoluteHashedPath.toFile().exists()) {
            return FileUtil.getRelativePath(absoluteHashedPath);
        }

        if (!file.renameTo(absoluteHashedPath.toFile())) {
            try {
                Files.copy(file.toPath(), absoluteHashedPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패", e);
            }
        }

        return FileUtil.getRelativePath(absoluteHashedPath);
    }
}