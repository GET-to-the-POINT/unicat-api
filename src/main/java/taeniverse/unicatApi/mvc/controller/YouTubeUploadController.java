package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.service.YouTubeUploadService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/youtube")
public class YouTubeUploadController {

    private final YouTubeUploadService youTubeUploadService;

    @PostMapping(value = "/{videoId}", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadVideo(
            @PathVariable("videoId") String videoId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient){
        return youTubeUploadService.uploadVideo(videoId, authorizedClient.getAccessToken(), title, description);
    }
}
