package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.application.service.voice.TTSService;
import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.domain.dto.project.SectionResponse;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class ArtifactService {

    private final YoutubeUploadService youtubeUploadService;
    private final MediaService mediaService;
    private final ProjectService projectService;
    private final SectionService sectionService;
    private final StorageService storageService;
    private final TTSService ttsService;
    private final OpenAiService openAiService;


    public void build(Long projectId) {
        this.build(projectId, "artifact", null);
    }

    // TODO: type을 String 으로 받으면 오타 나기 쉬움 밸리데이션 추가 필요, 또는 이넘으로 관리 필요

    public void build(Long projectId, String type, OAuth2AccessToken accessToken) {
        Project project = buildAndUpdate(projectId);
        if ("youtube".equals(type) && accessToken != null) {
            uploadSocial(project, type, accessToken);
        }
    }

    @Async
    public CompletableFuture<Void> buildAsync(Long projectId, String type, OAuth2AccessToken accessToken) {
        this.build(projectId, type, accessToken);
        return CompletableFuture.completedFuture(null);
    }


    private Project buildAndUpdate(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        List<SectionResponse> sectionResponses = sectionService.getAll(projectId);

        // section build standby & upload
        for (SectionResponse sectionResponse : sectionResponses) {
            sectionBuildAndUpload(sectionResponse.id());
        }
        sectionResponses = sectionService.getAll(projectId);

        // project build standby
        List<String> sectionVideoUrls = sectionResponses.stream()
                .map(SectionResponse::videoUrl)
                .toList();
        List<File> sectionVideos = storageService.downloads(sectionVideoUrls);

        // artifact build
        File artifactFile = mediaService.mergeVideosAndExtractVFR(sectionVideos);

        // artifact upload
        String uploadedUrl = storageService.upload(artifactFile);
        project.setArtifactUrl(uploadedUrl);

        return projectService.update(project);
    }


    private void sectionBuildAndUpload(Long sectionId) {
        Section section = sectionService.getOrElseThrow(sectionId);

        // video standby
        String videoUrl = section.getVideoUrl();
        if (StringUtils.hasText(videoUrl)) {
            storageService.download(videoUrl);
            return;
        }

        // resource standby
        String resourceUrl = section.getContentUrl(); // 리소스는 프로세스상 사용자가 선행하여 업로드한다.(인공지능생성도 선행되어서 진해오딘다)

        // audio standby, TODO, 메서드 분리가 필요
        String audioUrl = section.getAudioUrl();
        if (!StringUtils.hasText(audioUrl)) {
            File voiceFile = ttsService.create(section.getScript(), section.getVoiceModel());
            audioUrl = storageService.upload(voiceFile);
            section.setAudioUrl(audioUrl);
        }

        // video standby & build
        Project project = section.getProject();
        File templateResource = storageService.download(project.getTemplateUrl());
        File contentResource = storageService.download(resourceUrl);
        File audioResource = storageService.download(audioUrl);

        File titleResource = null;
        if (project.getTitleUrl() != null && !project.getTitleUrl().isBlank()) {
            titleResource = storageService.download(project.getTitleUrl());
        }

        File sectionVideoFile;
        if (titleResource != null) {
            sectionVideoFile = mediaService.mergeImageAndAudio(templateResource, contentResource, titleResource, audioResource);
        } else {
            sectionVideoFile = mediaService.mergeImageAndAudio(templateResource, contentResource, audioResource);
        }

        // video upload process
        String sectionVideoUrl = storageService.upload(sectionVideoFile);
        section.setVideoUrl(sectionVideoUrl);
        sectionService.update(section);
    }

    private void uploadSocial(Project project, String type, OAuth2AccessToken accessToken) {
        if ("youtube".equalsIgnoreCase(type)) {
            youtubeUploadService.uploadToYoutube(project, accessToken);
        }
    }


    @Transactional
    @Async
    public CompletableFuture<Long> oneStepAutoArtifact(Long memberId, PromptRequest promptRequest) {
        ProjectResponse projectResponse = projectService.create(memberId);
        Long projectId = projectResponse.id();
        for(int i = 0; i < 5; i++) {
            sectionService.create(projectId);
        }
        openAiService.oneStepCreateResource(projectId, promptRequest);
        Project project = projectService.getOrElseThrow(projectId);
        projectService.update(project);
        return CompletableFuture.completedFuture(projectId);
    }
}