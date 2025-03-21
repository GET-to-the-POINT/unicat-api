package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class SupabaseStorageServiceImpl extends AbstractStorageService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final MessageSource messageSource;

    @Override
    protected File realDownload(String fileUrl) {
        String fileName = Paths.get(URI.create(fileUrl).getPath()).getFileName().toString();
        File foundFile = FileUtil.getFile(fileName);
        if (foundFile.exists()) {
            return foundFile;
        }
        if (!foundFile.exists()) {
            try {
                Path parentDir = foundFile.toPath().getParent();
                if (parentDir != null) Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new UncheckedIOException("캐시 디렉토리 생성 실패: " + foundFile.getAbsolutePath(), e);
            }
            try {
                URL url = URI.create(fileUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Files.copy(inputStream, foundFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                } else {
                    throw new HttpClientErrorException(HttpStatus.valueOf(connection.getResponseCode()), "파일 다운로드 실패. HTTP 응답 코드: " + connection.getResponseCode());
                }
            } catch (IOException e) {
                throw new UncheckedIOException("파일 다운로드 오류: " + fileUrl, e);
            }
        }
        return foundFile;
    }


    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 제공해주세요.");
        }

        Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "unicat_uploads");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 업로드 디렉토리 생성 실패", e);
        }

        String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        File tmpFile;
        try {
            tmpFile = Files.createTempFile(baseDir, "upload-", extension).toFile();
            file.transferTo(tmpFile);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 파일 생성 실패", e);
        }

        String uniqueFileName = tmpFile.getName();
        String supabaseKey = appProperties.supabase().key();
        String bucket = getBucketName(file.getContentType());
        String key = "uploads/" + uniqueFileName;
        String url = getUrl(bucket, key);

        try {
            byte[] fileBytes = Files.readAllBytes(tmpFile.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file", e);
        } catch (RestClientException e) {
            log.error(e.getMessage());
            String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
        return url;
    }

    @Override
    public String upload(File file) {
        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch(IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if(contentType == null || !contentType.contains("/")) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        MultipartFile multipartFile = new MultipartFileUtil(file, file.getName(), contentType);
        return upload(multipartFile);
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


    private String getUrl(String bucket, String key) {
        String supabaseUrl = appProperties.supabase().url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", bucket, key)
                .build()
                .toUriString();
    }
}
