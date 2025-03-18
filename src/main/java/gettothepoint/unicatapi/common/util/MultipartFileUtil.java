package gettothepoint.unicatapi.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RequiredArgsConstructor
public class MultipartFileUtil implements MultipartFile {


    private final String name;
    private final String contentType;
    private final File file;
    private final byte[] data;

    public MultipartFileUtil(@NonNull File file, @NonNull String name, String contentType) {
        this.file = file;
        this.name = name;
        this.contentType = contentType;
        this.data = null;
    }

    public MultipartFileUtil(@NonNull byte[] data, @NonNull String name, String contentType) {
        this.data = data;
        this.name = name;
        this.contentType = contentType;
        this.file = null;
    }

    @Override
    @NonNull
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return file != null ? file.getName() : this.name;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return (data != null ? data.length == 0 : Objects.requireNonNull(file).length() == 0);
    }

    @Override
    public long getSize() {
        return (data != null) ? data.length : Objects.requireNonNull(file).length();
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        return (data != null) ? data : Files.readAllBytes(Objects.requireNonNull(file).toPath());
    }

    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return (data != null) ? new ByteArrayInputStream(data) : new FileInputStream(Objects.requireNonNull(file));
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        if (data != null) {
            Files.write(dest.toPath(), data);
        } else {
            Files.copy(Objects.requireNonNull(file).toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
