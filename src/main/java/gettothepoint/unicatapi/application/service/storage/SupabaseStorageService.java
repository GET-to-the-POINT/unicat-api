package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;
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
    public String uploadFile(MultipartFile file) {
        String uniqueFileName = generateUniqueFileName(file);
        String supabaseKey = appProperties.supabase().key();
        String bucket = getBucketName(file.getContentType());

        String key = "uploads/" + uniqueFileName;
        String url = getUrl(bucket, key);

        try {
            byte[] fileBytes = file.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return url;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
        } catch (RestClientException e) {
            log.error(e.getMessage());
            String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    private String getBucketName(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return appProperties.supabase().storage().bucket();
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("audio/")) {
            return "voice";
        } else if (contentType.startsWith("video/")) {
            return "video";
        }
        return appProperties.supabase().storage().bucket();
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = "";

        if (originalFileName.lastIndexOf(".") != -1) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        return UUID.randomUUID() + extension;
    }

    private String getUrl(String bucket, String key) {
        String supabaseUrl = appProperties.supabase().url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", bucket, key)
                .build()
                .toUriString();
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
