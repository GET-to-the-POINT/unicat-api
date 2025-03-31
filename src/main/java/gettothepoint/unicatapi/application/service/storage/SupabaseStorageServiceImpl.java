package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.domain.repository.SupabaseFileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
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
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static gettothepoint.unicatapi.common.util.FileUtil.getTempPath;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageServiceImpl extends AbstractStorageService {

    private final SupabaseProperties supabaseProperties;
    private final SupabaseFileStorageRepository supabaseFileStorageRepository;

    @Override
    protected File realDownload(String url) {
        return supabaseFileStorageRepository.getFile(url);
    }

    @Override
    public String upload(MultipartFile file) {
        log.info("업로드 요청: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.error("업로드할 파일이 null 이거나 비어 있습니다.");
            throw new IllegalArgumentException("업로드할 파일을 제공해주세요.");
        }

        Path baseDir = getTempPath();
        try {
            log.info("임시 업로드 디렉토리 생성: {}", baseDir);
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            log.error("임시 업로드 디렉토리 생성 실패: {}", baseDir, e);
            throw new UncheckedIOException("임시 업로드 디렉토리 생성 실패", e);
        }

        log.info("업로드할 파일: {}", file.getOriginalFilename());
        String extension = Optional.ofNullable(file.getOriginalFilename()).filter(name -> name.lastIndexOf('.') != -1).map(name -> name.substring(name.lastIndexOf('.'))).orElse("");

        File tmpFile;
        try {
            log.info("임시 파일 생성: {}", extension);
            tmpFile = Files.createTempFile(baseDir, "upload-", extension).toFile();
            file.transferTo(tmpFile);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 파일 생성 실패", e);
        }

        return supabaseFileStorageRepository.saveFile(tmpFile.getAbsolutePath());
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
        return supabaseFileStorageRepository.saveFile(file.getAbsolutePath());
    }


    public String getBucketName(String contentType) {
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
                String baseUrl = supabaseProperties.url();
                String bucketUrlPrefix = "/storage/v1/object/";
                String fixedPrefix = "public/assets/template/";
                url = baseUrl + bucketUrlPrefix + fixedPrefix + url;
            }

            URL downloadUrl = URI.create(url).toURL();
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                throw new HttpClientErrorException(HttpStatus.valueOf(responseCode), "파일 다운로드 실패. HTTP 응답 코드: " + responseCode);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("파일 다운로드 오류: " + url, e);
        }
    }
}
