package taeniverse.unicatApi.mvc.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import taeniverse.unicatApi.mvc.service.YouTubeService;

@AllArgsConstructor
@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youtubeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("filePath") String filePath,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestHeader("Authorization")
            @Parameter(hidden = true) String authorization
    ) {

        System.out.println("Received Authorization Header: " + authorization);

        // 액세스 토큰 확인 (헤더에서 "Bearer " 제거)
        String accessToken = authorization.replace("Bearer ", "");

        try {
            // 비디오 업로드
            youtubeService.uploadVideo(filePath, accessToken, title, description);
            return ResponseEntity.ok("비디오 업로드 성공!");
        } catch (Exception e) {
            // 업로드 실패 시 500 에러와 함께 상세 메시지 반환
            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
        }
    }
}
