package gettothepoint.unicatapi.filestorage.infrastructure.domain;

import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.domain.policy.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.policy.StoredFileValidator;
import gettothepoint.unicatapi.filestorage.domain.service.FileResourceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class FileResourceFactoryImpl implements FileResourceFactory {

    private final StoredFileValidator storedFileValidator;
    private final FileNameTransformer fileNameTransformer;

    private FileResource create(String filename, InputStream content, long size, String contentType) {
        if (storedFileValidator == null || fileNameTransformer == null) {
            throw new IllegalStateException("StoredFile is not configured");
        }

        storedFileValidator.validate(filename, content, size, contentType);
        filename = fileNameTransformer.transform(filename, content);

        return new FileResource(filename, content, size, contentType);
    }

    /**
     * MultipartFile에서 StoredFile 객체 생성
     */
    @Override
    public FileResource fromMultipartFile(MultipartFile file, Path directory) {
        try {
            // 파일 내용 읽기
            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            // 해시 파일명 생성
            String fullPath = directory.resolve(file.getOriginalFilename()).toString().replace(File.separator, "/");

            // StoredFile 객체 생성
            return create(fullPath, new ByteArrayInputStream(bytes), bytes.length, file.getContentType());
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
    }

    /**
     * File 객체에서 StoredFile 객체 생성
     */
    @Override
    public FileResource fromFile(File file, Path directory) {
        try {
            // 파일 내용 읽기
            byte[] bytes = Files.readAllBytes(file.toPath());

            // 콘텐츠 타입 감지
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // 기본값
            }

            // 해시 파일명 생성
            String fullPath = directory.resolve(file.getName()).toString().replace(File.separator, "/");

            // StoredFile 객체 생성
            return create(fullPath, new ByteArrayInputStream(bytes), bytes.length, contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
    }
}