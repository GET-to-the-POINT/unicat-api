package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.storage.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommandValidator;
import lombok.Builder;

import java.io.InputStream;

public final class DefaultFileStorageCommand extends FileStorageCommand {

    private static FileStorageCommandValidator fileStorageCommandValidator;
    private static FileNameTransformer fileNameTransformer;

    public static void configure(FileStorageCommandValidator validator, FileNameTransformer transformer) {
        fileStorageCommandValidator = validator;
        fileNameTransformer = transformer;
    }

    @Builder
    private DefaultFileStorageCommand(String filename, InputStream content, long size, String contentType) {
        if (fileStorageCommandValidator == null || fileNameTransformer == null) {
            throw new IllegalStateException("FileStorageCommand is not configured");
        }

        fileStorageCommandValidator.validate(filename, content, size, contentType);
        filename = fileNameTransformer.transform(filename, content);

        super(filename, content, size, contentType);
    }

}