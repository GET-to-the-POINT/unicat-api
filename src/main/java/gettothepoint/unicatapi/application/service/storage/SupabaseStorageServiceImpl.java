package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

import static gettothepoint.unicatapi.common.util.FileUtil.getTempPath;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class SupabaseStorageServiceImpl extends AbstractStorageService {

    private final SupabaseProperties supabaseProperties;

    private final RestTemplate restTemplate;

    @Override
    protected File realDownload(String url) {
        File targetFile = FileUtil.getTemp(url);

        // 여기에 진입한 경우는 무조건 파일이 없을 경우 입니다.
        // 추상 클래스를 참조하십시오.
        try {
            Path parentDir = targetFile.toPath().getParent();
            if (parentDir != null) Files.createDirectories(parentDir);
        } catch (IOException e) {
            throw new UncheckedIOException("캐시 디렉토리 생성 실패: " + targetFile.getAbsolutePath(), e);
        }
        downloadToFile(url, targetFile);
        return targetFile;
    }

    @Override
    public String upload(MultipartFile file) {
        log.info("업로드 요청: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 제공해주세요.");
        }

        Path baseDir = getTempPath();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 업로드 디렉토리 생성 실패", e);
        }

        String extension = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> name.lastIndexOf('.') != -1)
                .map(name -> name.substring(name.lastIndexOf('.')))
                .orElse("");

        File tmpFile;
        try {
            tmpFile = Files.createTempFile(baseDir, "upload-", extension).toFile();
            file.transferTo(tmpFile);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 파일 생성 실패", e);
        }

        String supabaseKey = supabaseProperties.key();
        String bucket = getBucketName(file.getContentType());
        String key = "uploads/" + tmpFile.getName();
        String url = getUrl(bucket, key);

        try {
            byte[] fileBytes = Files.readAllBytes(tmpFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("업로드 성공: {}", response.getBody());
                return url;
            } else {
                log.error("업로드 실패: {}", response.getBody());
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "업로드 실패. 상태 코드: " + response.getStatusCode()
                );
            }

        } catch (IOException e) {
            throw new UncheckedIOException("파일 업로드 중 IO 오류 발생", e);
        }
    }

    @Override
    public String upload(File file) {
        String contentType;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (contentType == null || !contentType.contains("/")) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        MultipartFile multipartFile = new MultipartFileUtil(file, file.getName(), contentType);
        return upload(multipartFile);
    }


    private String getBucketName(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return supabaseProperties.storage().bucket();
        }
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("audio/")) {
            return "voice";
        } else if (contentType.startsWith("video/")) {
            return "video";
        }
        return supabaseProperties.storage().bucket();
    }


    private String getUrl(String bucket, String key) {
        String supabaseUrl = supabaseProperties.url();
        return UriComponentsBuilder.fromUriString(supabaseUrl).pathSegment("storage", "v1", "object", bucket, key).build().toUriString();
    }

    private void downloadToFile(String url, File targetFile) {
        try {
            if (!url.startsWith("http")) {
                url = supabaseProperties.url() + "/storage/v1/object/public/assets/template/" + url;
            }

            Resource resource = restTemplate.getForObject(url, Resource.class);
            if (resource == null) {
                throw new HttpClientErrorException(HttpStatus.NO_CONTENT, "리소스가 비어 있습니다.");
            }

            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("파일 다운로드 오류: " + url, e);
        }
    }
}
