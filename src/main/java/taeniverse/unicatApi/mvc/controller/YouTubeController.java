package taeniverse.unicatApi.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.YouTubeService;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;

    @PostMapping("/upload")
    public String uploadVideo(@RequestParam String videoFilePath,  // 파일 경로 받기
                              @RequestParam String videoFileName) {  // 파일 이름 받기
        try {
            // 서비스 호출해서 동영상 업로드
            youTubeService.uploadVideo(videoFilePath, videoFileName);
            return "Video uploaded successfully!";
        } catch (Exception e) {
            return "Error uploading video: " + e.getMessage();
        }
    }
}
