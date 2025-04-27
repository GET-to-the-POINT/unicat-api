package gettothepoint.unicatapi.filestorage.domain.storage;

import lombok.Builder;
import org.apache.commons.codec.binary.Hex;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Builder
public class StoredFile {

    private final String filename;
    private final InputStream content;
    private final long size;
    private final String contentType;

    public FileStorageCommand toCommand() {
        return FileStorageCommand.builder()
                .filename(filename)
                .content(content)
                .size(size)
                .contentType(contentType)
                .build();
    }

    public static StoredFile fromRaw(InputStream input, String originalFilename, long size, String contentType, Path path) {
        try {
            byte[] bytes = input.readAllBytes();
            String hash = sha256(bytes);
            String ext = getExtension(originalFilename);
            String filename = hash + (ext.isEmpty() ? "" : "." + ext);
            String hashedFilename = path.resolve(filename).toString().replace(File.separator, "/");

            return StoredFile.builder()
                    .filename(hashedFilename)
                    .content(new ByteArrayInputStream(bytes))
                    .size(size)
                    .contentType(contentType)
                    .build();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StoredFile fromMultipart(MultipartFile file, Path path) {
        try {
            return fromRaw(file.getInputStream(), file.getOriginalFilename(), file.getSize(), file.getContentType(), path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static StoredFile fromFile(File file, String contentType, Path path) {
        try {
            return fromRaw(new FileInputStream(file), file.getName(), file.length(), contentType, path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Hex.encodeHexString(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
