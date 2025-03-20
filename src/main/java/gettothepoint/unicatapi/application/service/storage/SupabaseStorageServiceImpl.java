//package gettothepoint.unicatapi.application.service.storage;
//
//import gettothepoint.unicatapi.common.propertie.AppProperties;
//import gettothepoint.unicatapi.domain.dto.storage.UploadResult;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.MessageSource;
//import org.springframework.context.i18n.LocaleContextHolder;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.util.Objects;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SupabaseStorageServiceImpl extends AbstractStorageService {
//
//    private final AppProperties appProperties;
//    private final RestTemplate restTemplate;
//    private final MessageSource messageSource;
//
//    @Override
//    public UploadResult upload(File inputStream, String fileMimeType) {
//        // TODO : 구현하기
//        return null;
//    }
//
//    @Override
//    public UploadResult upload(InputStream inputStream, String fileMimeType) {
//        // TODO : 구현하기
//        return null;
//    }
//
//    @Override
//    public UploadResult upload(MultipartFile file, String fileMimeType) {
//        String uniqueFileName = generateUniqueFileName(file);
//        String supabaseKey = appProperties.supabase().key();
//        String bucket = getBucketName(file.getContentType());
//        String key = "uploads/" + uniqueFileName;
//        String url = getUrl(bucket, key);
//        try {
//            byte[] fileBytes = file.getBytes();
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("apikey", supabaseKey);
//            headers.set("Authorization", "Bearer" + supabaseKey);
//            headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
//            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
//            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
//            int fileHash = java.util.Arrays.hashCode(fileBytes);
//            String mime = file.getContentType();
//            return new UploadResult(fileHash, url, mime);
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
//        } catch (RestClientException e) {
//            log.error(e.getMessage());
//            String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
//        }
//    }
//
//    private String getBucketName(String contentType) {
//        if (contentType == null || contentType.isBlank()) {
//            return appProperties.supabase().storage().bucket();
//        }
//        if (contentType.startsWith("multipartFile/")) {
//            return "image";
//        } else if (contentType.startsWith("audio/")) {
//            return "voice";
//        } else if (contentType.startsWith("video/")) {
//            return "video";
//        }
//        return appProperties.supabase().storage().bucket();
//    }
//
//    private String generateUniqueFileName(MultipartFile file) {
//        String originalFileName = sanitizeFileName(file.getOriginalFilename());
//        String extension = "";
//
//        if (originalFileName.lastIndexOf(".") != -1) {
//            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//        }
//
//        return UUID.randomUUID() + extension;
//    }
//
//    private String getUrl(String bucket, String key) {
//        String supabaseUrl = appProperties.supabase().url();
//        return UriComponentsBuilder.fromUriString(supabaseUrl)
//                .pathSegment("storage", "v1", "object", bucket, key)
//                .build()
//                .toUriString();
//    }
//
//    private String sanitizeFileName(String fileName) {
//        if (fileName == null || fileName.isBlank()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
//        }
//
//        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
//    }
//
//    public File downloadFile(String fileUrl) throws IOException {
//        ResponseEntity<byte[]> response = restTemplate.exchange(
//                fileUrl,
//                HttpMethod.GET,
//                new HttpEntity<>(new HttpHeaders()),
//                byte[].class
//        );
//        if (response.getStatusCode() != HttpStatus.OK) {
//            throw new IOException("Supabase에서 파일 다운로드 실패: " + fileUrl);
//        }
//        File tempFile = File.createTempFile("supabase_video_", ".mp4");
//        Files.write(tempFile.toPath(), Objects.requireNonNull(response.getBody()));
//        log.info("✅ Supabase에서 다운로드 완료: {}", tempFile.getAbsolutePath());
//        return tempFile;
//    }
//
//
//    @Override
//    protected InputStream realDownload(Integer fileHash) {
//        return null;
//    }
//}
