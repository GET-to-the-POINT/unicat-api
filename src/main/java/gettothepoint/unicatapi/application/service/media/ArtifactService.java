package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.domain.dto.storage.UploadResult;
import gettothepoint.unicatapi.domain.dto.project.SectionResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArtifactService {

    private final YoutubeUploadService youtubeUploadService;
    private final MediaService mediaService;
    private final ProjectService projectService;
    private final SectionService sectionService;
    private final StorageService storageService;
    private final TextToSpeechService textToSpeechService;

    public void build(Long projectId) {
        this.build(projectId, "artifact", null);
    }

    // TODO: type을 String 으로 받으면 오타 나기 쉬움 밸리데이션 추가 필요, 또는 이넘으로 관리 필요
    public void build(Long projectId, String type, OAuth2AccessToken accessToken) {
        Project project = buildAndUpdate(projectId);
        uploadSocial(project, type, accessToken);
    }

    private Project buildAndUpdate(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        List<SectionResponse> sections = sectionService.getAll(projectId);

        // build
        for (SectionResponse sectionResponse : sections) {
            sectionBuild(sectionResponse.id());
        }
        List<String> sectionVideoHashCodes = sections.stream().map(SectionResponse::videoHashCode).toList();
        List<InputStream> sectionVideoStreams = storageService.downloads(sectionVideoHashCodes);
        InputStream artifactStream = mediaService.mergeVideosAndExtractVFRFromInputStream(sectionVideoStreams);
        UploadResult artifactUploadResult = storageService.upload(artifactStream, "video/mp4");
        project.setArtifactHashCode(artifactUploadResult.fileHashCode());
        project.setArtifactMimeType(artifactUploadResult.mimeType());
        project.setArtifactUrl(artifactUploadResult.url());
        return projectService.update(project);
    }

    private void sectionBuild(Long sectionId) {
        Section section = sectionService.getOrElseThrow(sectionId);
        String resourceHashCode = section.getResourceHashCode(); // 리소스는 프로세스상 사용자가 선행하여 업로드한다.(인공지능생성도 선행되어서 진해오딘다)

        // 오디오 생성
        if (section.getAudioHashCode() == null) {
            InputStream voiceStream = textToSpeechService.create(section.getScript(), section.getVoiceModel());
            UploadResult uploadResult = storageService.upload(voiceStream, "audio/mp3"); // TODO : mimeType
            section.setAudioUrl(uploadResult.url());
            section.setAudioHashCode(uploadResult.fileHashCode());
            section.setAudioMimeType(uploadResult.mimeType());
        }
        String audioHashCode = section.getAudioHashCode();

        // 비디오 생성
        InputStream resourceStream = storageService.download(resourceHashCode);
        InputStream audioStream = storageService.download(audioHashCode);
        InputStream sectionVideo = mediaService.mergeImageAndSound(resourceStream, audioStream);

        UploadResult uploadResult = storageService.upload(sectionVideo, "video/mp4"); // TODO : mimeType 을 동적으로 적용하기
        section.setVideoHashCode(uploadResult.fileHashCode());
        section.setVideoUrl(uploadResult.url());
        section.setVideoMimeType(uploadResult.mimeType());
        sectionService.update(section);
    }

    private void uploadSocial(Project project, String type, OAuth2AccessToken accessToken) {
        try {
            if ("youtube".equalsIgnoreCase(type)) {
                youtubeUploadService.uploadVideoToYoutube(project, accessToken);
            } else if ("vimeo".equalsIgnoreCase(type)) {
                // TODO: 과연 나중에 생길까?
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload artifact", e);
        }
    }

}