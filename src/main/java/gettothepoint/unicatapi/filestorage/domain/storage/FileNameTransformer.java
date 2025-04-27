package gettothepoint.unicatapi.filestorage.domain.storage;

import java.io.InputStream;

public interface FileNameTransformer {
    String transform(String filename, InputStream content);
}