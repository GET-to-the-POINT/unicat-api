package gettothepoint.unicatapi.filestorage.infrastructure.persistence.composite;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.infrastructure.exception.CompositeFileStorageException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CompositeFileStorageRepository implements FileStorageRepository {

    private final List<FileStorageRepository> delegates;

    @Override
    public String store(FileResource file) {
        if (delegates.isEmpty()) {
            throw CompositeFileStorageException.noDelegates();
        }

        List<Throwable> failures = new ArrayList<>();
        boolean atLeastOneSuccess = false;

        for (FileStorageRepository delegate : delegates) {
            try {
                delegate.store(file);
                atLeastOneSuccess = true;
            } catch (Throwable e) {
                failures.add(e);
            }
        }

        if (!atLeastOneSuccess) {
            throw CompositeFileStorageException.completeFailure(failures);
        } else if (!failures.isEmpty()) {
            // 일부 성공, 일부 실패인 경우 - 로깅하고 계속 진행 (또는 정책에 따라 예외 발생 가능)
            // logger.warn("일부 저장소에 저장 실패: {}", failures);
        }

        return file.filename();
    }

    @Override
    public Optional<UrlResource> load(@NotNull String key) {
        if (delegates.isEmpty()) {
            return Optional.empty();
        }

        for (FileStorageRepository delegate : delegates) {
            Optional<UrlResource> resource = delegate.load(key);
            if (resource.isPresent()) {
                return resource;
            }
        }

        return Optional.empty();
    }
}