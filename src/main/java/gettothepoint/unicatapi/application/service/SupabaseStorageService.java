package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.StorageUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class SupabaseStorageService implements FileStorageService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final MessageSource messageSource;

    @Override
    public StorageUpload uploadFile(MultipartFile file) {
        String uniqueFileName = generateUniqueFileName(file);
        String supabaseUrl = appProperties.supabase().url();
        String supabaseKey = appProperties.supabase().key();
        String bucket = appProperties.supabase().storage().bucket();

        String key = "uploads/" + uniqueFileName;
        String url = String.format("%s/storage/v1/object/%s/%s?upsert=false", supabaseUrl, bucket, key);

        try {
            byte[] fileBytes = file.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            } catch (Exception e) {
                log.error(e.getMessage());
                String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
            }

            String uploadedUrl = getUrl(key);
            String originalFilename = file.getOriginalFilename();
            return new StorageUpload(uploadedUrl, originalFilename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.lastIndexOf(".") != -1) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }

    private String getUrl(String key) {

        String supabaseUrl = appProperties.supabase().url();
        String bucket = appProperties.supabase().storage().bucket();

        return String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, key);
    }
}
