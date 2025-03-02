package taeniverse.unicatApi.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.YouTubeService;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    @Autowired
    private YouTubeService youTubeService;

    /**
     * 동영상을 업로드하는 API 엔드포인트
     * @param videoFilePath  : 동영상 파일의 경로
     * @param videoFileName  : 동영상 파일 이름
     * @param headers        : HTTP 요청 헤더 (Authorization 헤더 포함)
     * @return 업로드 성공/실패 메시지
     */
    @PostMapping("/upload")
    public String uploadVideo(@RequestParam String videoFilePath,  // 파일 경로 받기
                              @RequestParam String videoFileName,  // 파일 이름 받기
                              @RequestHeader HttpHeaders headers) {  // Authorization 헤더 받기
        try {
            // Authorization 헤더가 없으면 예외 처리
            String authorizationHeader = headers.getFirst("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new Exception("Authorization header is missing or invalid.");
            }

            // Authorization 헤더에서 "Bearer " 부분을 제외한 액세스 토큰만 추출
            String accessToken = authorizationHeader.substring(7);

            // 토큰 추출 확인 로그
            System.out.println("Extracted Access Token: " + accessToken);

            // 엑세스 토큰을 통해 동영상을 업로드하는 서비스 호출
            youTubeService.uploadVideo(videoFilePath, videoFileName, accessToken);
            return "Video uploaded successfully!";
        } catch (Exception e) {
            return "Error uploading video: " + e.getMessage();
        }
    }
}
