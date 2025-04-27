package gettothepoint.unicatapi.filestorage.domain.policy;

import java.io.InputStream;

public interface StoredFileValidator {
    void validate(String filename, InputStream content, long size, String contentType);
}
