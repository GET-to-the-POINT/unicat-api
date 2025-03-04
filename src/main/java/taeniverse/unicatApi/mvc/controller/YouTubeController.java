package taeniverse.unicatApi.mvc.controller;

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
            @RequestParam("file") MultipartFile file, @RequestParam("accessToken") String accessToken,
        @RequestParam("title") String title, @RequestParam("description") String description){
        try {
            youtubeService.uploadVideo(file.getOriginalFilename(), accessToken, file , title, description);
            return ResponseEntity.ok("비디오 업로드 성공!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
        }
    }
}