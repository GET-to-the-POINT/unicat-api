package gettothepoint.unicatapi.filestorage.presentation;

import gettothepoint.unicatapi.filestorage.application.port.in.FileDownloadUseCase;
import gettothepoint.unicatapi.filestorage.application.port.in.FileUploadUseCase;
import gettothepoint.unicatapi.filestorage.presentation.dto.FileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadUseCase fileUploadUseCase;
    private final FileDownloadUseCase fileDownloadUseCase;

    /**
     * 파일 업로드 API
     * 
     * @param file 업로드할 파일
     * @return 파일 키를 포함한 응답
     */
    @PostMapping
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileKey = fileUploadUseCase.uploadFile(file);
        return ResponseEntity.ok(FileResponse.success(fileKey));
    }

    /**
     * 파일 다운로드 API
     * 
     * @param fileKey 다운로드할 파일 키
     * @param filename 다운로드 시 사용할 파일명 (선택적)
     * @return 파일 리소스 또는 404 응답
     */
    @GetMapping("/{fileKey}")
    public ResponseEntity<UrlResource> downloadFile(
            @PathVariable String fileKey,
            @RequestParam(value = "filename", required = false) String filename) {
        
        return fileDownloadUseCase.downloadFile(fileKey)
                .map(resource -> {
                    String contentDisposition = "attachment";
                    
                    // 파일명이 제공된 경우 Content-Disposition 헤더에 추가
                    if (filename != null && !filename.isEmpty()) {
                        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                                .replace("+", "%20");
                        contentDisposition += "; filename*=UTF-8''" + encodedFilename;
                    }
                    
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
