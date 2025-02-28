package taeniverse.unicatApi.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.YouTubeService;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;

    /**
     * 동영상을 업로드하는 API 엔드포인트
     * @param videoFilePath  : 동영상 파일의 경로
     * @param videoFileName  : 동영상 파일 이름
     * @param accessToken    : YouTube API에 필요한 엑세스 토큰
     * @return 업로드 성공/실패 메시지
     */
    @PostMapping("/upload")
    public String uploadVideo(@RequestParam String videoFilePath,  // 파일 경로 받기
                              @RequestParam String videoFileName,  // 파일 이름 받기
                              @RequestParam String accessToken) {  // 엑세스 토큰 받기
        try {
            System.out.println("Received Access Token: " + accessToken);
            // 엑세스 토큰을 통해 동영상을 업로드하는 서비스 호출
            youTubeService.uploadVideo(videoFilePath, videoFileName, accessToken);
            return "Video uploaded successfully!";
        } catch (Exception e) {
            return "Error uploading video: " + e.getMessage();
        }
    }
}

