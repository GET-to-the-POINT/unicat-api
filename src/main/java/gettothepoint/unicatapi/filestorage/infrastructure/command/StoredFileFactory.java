package gettothepoint.unicatapi.filestorage.infrastructure.command;

import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import gettothepoint.unicatapi.filestorage.domain.policy.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.policy.StoredFileValidator;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class StoredFileFactory {

    private static StoredFileValidator storedFileValidator;
    private static FileNameTransformer fileNameTransformer;

    public static void configure(StoredFileValidator validator, FileNameTransformer transformer) {
        storedFileValidator = validator;
        fileNameTransformer = transformer;
    }

    public static StoredFile create(String filename, InputStream content, long size, String contentType) {
        if (storedFileValidator == null || fileNameTransformer == null) {
            throw new IllegalStateException("StoredFile is not configured");
        }

        storedFileValidator.validate(filename, content, size, contentType);
        filename = fileNameTransformer.transform(filename, content);

        return new StoredFile(filename, content, size, contentType);
    }

    /**
     * MultipartFile에서 StoredFile 객체 생성
     */
    public static StoredFile fromMultipartFile(MultipartFile file, Path directory) {
        try {
            // 파일 내용 읽기
            InputStream inputStream = file.getInputStream();
            byte[] bytes = inputStream.readAllBytes();

            // 해시 파일명 생성
            String hash = sha256(bytes);
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            String filename = hash + (ext.isEmpty() ? "" : "." + ext);
            String fullPath = directory.resolve(filename).toString().replace(File.separator, "/");

            // StoredFile 객체 생성
            return StoredFileFactory.create(fullPath, new ByteArrayInputStream(bytes), bytes.length, file.getContentType());
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
    }

    /**
     * File 객체에서 StoredFile 객체 생성
     */
    public static StoredFile fromFile(File file, Path directory) {
        try {
            // 파일 내용 읽기
            byte[] bytes = Files.readAllBytes(file.toPath());

            // 콘텐츠 타입 감지
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // 기본값
            }

            // 해시 파일명 생성
            String hash = sha256(bytes);
            String ext = FilenameUtils.getExtension(file.getName());
            String filename = hash + (ext.isEmpty() ? "" : "." + ext);
            String fullPath = directory.resolve(filename).toString().replace(File.separator, "/");

            // StoredFile 객체 생성
            return StoredFileFactory.create(fullPath, new ByteArrayInputStream(bytes), bytes.length, contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 읽기 중 오류 발생", e);
        }
    }

    // SHA-256 해시 생성 메서드 (기존과 동일)
    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Hex.encodeHexString(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 지원하지 않습니다", e);
        }
    }
}