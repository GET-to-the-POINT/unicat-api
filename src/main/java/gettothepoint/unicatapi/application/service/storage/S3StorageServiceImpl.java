package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.repository.SupabaseFileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class S3StorageServiceImpl extends AbstractStorageService {

    private final SupabaseFileStorageRepository supabaseFileStorageRepository;
    private final FileStorageService fileStorageService;

    @Override
    public String upload(MultipartFile file) {
        log.info("업로드 요청: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.error("업로드할 파일이 null 이거나 비어 있습니다.");
            throw new IllegalArgumentException("업로드할 파일을 제공해주세요.");
        }

        return fileStorageService.storeMultipartFile(file);
    }

    @Override
    public String upload(File file) {
        return supabaseFileStorageRepository.save(file);
    }

    @Override
    protected File realDownload(String fileUrl) {
        return supabaseFileStorageRepository.getFile(fileUrl);
    }

}
