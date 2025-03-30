package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.domain.repository.SupabaseFileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
@Primary
public class SupabaseStorageServiceImpl extends AbstractStorageService {

    private final SupabaseProperties supabaseProperties;
    private final SupabaseFileStorageRepository supabaseFileStorageRepository;

    @Override
    protected File realDownload(String url) {
//        File targetFile = FileUtil.getTemp(url);
//
//        // 여기에 진입한 경우는 무조건 파일이 없을 경우 입니다.
//        // 추상 클래스를 참조하십시오.
//        try {
//            Path parentDir = targetFile.toPath().getParent();
//            if (parentDir != null) Files.createDirectories(parentDir);
//        } catch (IOException e) {
//            throw new UncheckedIOException("캐시 디렉토리 생성 실패: " + targetFile.getAbsolutePath(), e);
//        }
//    downloadToFile(url, targetFile);
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
        String extension = Optional.ofNullable(file.getOriginalFilename())
                .filter(name -> name.lastIndexOf('.') != -1)
                .map(name -> name.substring(name.lastIndexOf('.')))
                .orElse("");

        File tmpFile;
        try {
            log.info("임시 파일 생성: {}", extension);
            tmpFile = Files.createTempFile(baseDir, "upload-", extension).toFile();
            file.transferTo(tmpFile);
        } catch (IOException e) {
            throw new UncheckedIOException("임시 파일 생성 실패", e);
        }

//        String supabaseKey = supabaseProperties.key();
        String bucket = getBucketName(file.getContentType());
        String key = "uploads/" + tmpFile.getName();
//        String url = getUrl(bucket, key);

        return supabaseFileStorageRepository.saveFile(tmpFile.getAbsolutePath());

//        @SuppressWarnings("java:S2095")
//        HttpClient httpClient = HttpClient.newHttpClient();
//
//        try {
//            log.info("업로드 요청 URL: {}", url);
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(url))
//                    .header("apikey", supabaseKey)
//                    .header("Authorization", "Bearer " + supabaseKey)
//                    .header("Content-Type", Objects.requireNonNull(file.getContentType()))
//                    .POST(HttpRequest.BodyPublishers.ofFile(tmpFile.toPath()))
//                    .build();
//
//            log.info("업로드 요청 전송: {}", request);
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            if (response.statusCode() >= 400) {
//                log.error("업로드 실패: {}", response.body());
//                throw new ResponseStatusException(
//                        HttpStatus.INTERNAL_SERVER_ERROR,
//                        String.format("업로드 실패, 상태코드: %d, 메시지: %s", response.statusCode(), response.body())
//                );
//            }
//
//            log.info("업로드 성공: {}", response.body());
//            return url;
//        } catch (IOException e) {
//            log.error("업로드 요청 중 IO 오류 발생", e);
//            throw new UncheckedIOException("파일 업로드 중 IO 오류 발생", e);
//        } catch (InterruptedException e) {
//            log.error("업로드 요청이 중단되었습니다.", e);
//            Thread.currentThread().interrupt();
//            throw new IllegalStateException("업로드 요청이 중단되었습니다.", e);
//        }
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
