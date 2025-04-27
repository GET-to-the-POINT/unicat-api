package gettothepoint.unicatapi.filestorage.domain.storage;

import java.io.InputStream;

public interface FileStorageCommandValidator {
    void validate(String filename, InputStream content, long size, String contentType);
}
