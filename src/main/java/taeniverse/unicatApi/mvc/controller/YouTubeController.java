package taeniverse.unicatApi.mvc.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.YouTubeService;
import io.swagger.v3.oas.annotations.Parameter;

@AllArgsConstructor
@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadVideo(
            @RequestHeader("Authorization")
            @Parameter(hidden = true) String accessToken, // 🔹 헤더에서 받기
            @RequestParam("videoId") String videoId,
            @RequestParam("title") String title,
            @RequestParam("description") String description) {

        try {
            // Bearer 토큰 형식이면 "Bearer " 부분 제거
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            // 🔹 YouTube 업로드 서비스 호출
            String videoUrl = youTubeService.uploadVideo(videoId, accessToken, title, description);
            return ResponseEntity.ok("Upload successful! Video URL: " + videoUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed: " + e.getMessage());
        }
    }
}





















//public class YouTubeController {
//
//    private final YouTubeService youtubeService;
//
//    @PostMapping(value = "/upload", consumes = "application/json")
//    public ResponseEntity<String> uploadVideo(
//            @RequestParam("videoId") String videoId,
//            @RequestParam("title") String title,
//            @RequestParam("description") String description,
//            @RequestHeader("Authorization")
//            @Parameter(hidden = true) String authorization
//    ) {
//
//        System.out.println("Received Authorization Header: " + authorization);
//
//        // 액세스 토큰 확인 (헤더에서 "Bearer " 제거)
//        String accessToken = authorization.replace("Bearer ", "");
//
//        try {
//            // 비디오 업로드
//            youtubeService.uploadVideo(videoId, accessToken, title, description);
//            return ResponseEntity.ok("비디오 업로드 성공!");
//        } catch (Exception e) {
//            // 업로드 실패 시 500 에러와 함께 상세 메시지 반환
//            return ResponseEntity.status(500).body("업로드 실패: " + e.getMessage());
//        }
//    }
//}
