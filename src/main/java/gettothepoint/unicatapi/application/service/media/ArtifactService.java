package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.ai.OpenAiService;
import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.application.service.voice.TTSService;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.domain.dto.project.project.ProjectResponse;
import gettothepoint.unicatapi.domain.entity.project.Project;
import gettothepoint.unicatapi.domain.entity.project.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

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
    private final TransitionSoundService transitionSoundService;


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

    private Project buildAndUpdate(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);

        if (StringUtils.hasText(project.getArtifactKey())) {
            return project;
        }

        List<Section> sections = sectionService.getSectionAll(projectId);

        // section build standby & upload
        for (Section section : sections) {
            sectionBuildAndUpload(project.getId(), section.getId());
        }
        sections = sectionService.getSectionAll(projectId);

        // project build standby
        List<String> sectionKeys = sections.stream()
                .map(Section::getFrameKey)
                .toList();
        List<File> sectionVideos = storageService.getAll(sectionKeys);
        List<Section> sectionEntities = sectionService.getSectionAll(projectId);
        List<File> transitionSounds = transitionSoundService.downloadTransitionSoundsFromSections(sectionEntities);

        File artifactFile = mediaService.mergeVideosAndExtractVFR(sectionVideos, transitionSounds);

        // artifact upload
        String uploadedUrl = storageService.save(artifactFile);
        project.setArtifactKey(uploadedUrl);

        return projectService.update(project);
    }

    private void sectionBuildAndUpload(Long projectId, Long sectionId) {
        Section section = sectionService.getOrElseThrow(sectionId);

        // 비디오 체크 및 생성
        String videoKey = section.getFrameKey();
        if (StringUtils.hasText(videoKey)) {
            storageService.get(videoKey);
            return;
        }

        // 오디오 체크 및 생성
        String audioKey = section.getAudioKey();
        if (!StringUtils.hasText(audioKey)) {
            File voiceFile = ttsService.create(section.getScript(), section.getVoiceModel());
            audioKey = storageService.save(voiceFile);
            section.setAudioKey(audioKey);
        }

        // 콘텐츠 체크 및 생성
        String contentKey = section.getContentKey();
        if (!StringUtils.hasText(contentKey)) {
            PromptRequest promptRequest = new PromptRequest(section.getScript());
            openAiService.createResource(projectId, sectionId, "image", promptRequest);
            contentKey = sectionService.getOrElseThrow(sectionId).getContentKey();
        }

        // video standby & build
        Project project = section.getProject();
        File templateResource = storageService.get(project.getTemplateKey());
        File contentResource = storageService.get(contentKey);
        File audioResource = storageService.get(audioKey);

        File titleResource = null;
        if (project.getTitleImageKey() != null && !project.getTitleImageKey().isBlank()) {
            titleResource = storageService.get(project.getTitleImageKey());
        }

        File sectionVideoFile;
        if (titleResource != null) {
            sectionVideoFile = mediaService.mergeImageAndAudio(templateResource, contentResource, titleResource, audioResource);
        } else {
            sectionVideoFile = mediaService.mergeImageAndAudio(templateResource, contentResource, audioResource);
        }

        // video upload process
        String sectionVideoUrl = storageService.save(sectionVideoFile);
        section.setFrameKey(sectionVideoUrl);
        sectionService.update(section);
    }

    private void uploadSocial(Project project, String type, OAuth2AccessToken accessToken) {
        if ("youtube".equalsIgnoreCase(type)) {
            youtubeUploadService.uploadToYoutube(project, accessToken);
        }
    }


    @Transactional
    public Long oneStepAutoArtifact(Long memberId, PromptRequest promptRequest) {
        ProjectResponse projectResponse = projectService.create(memberId);
        Long projectId = projectResponse.id();
        for(int i = 0; i < 5; i++) {
            sectionService.create(projectId);
        }
        openAiService.oneStepCreateResource(projectId, promptRequest);
        Project project = projectService.getOrElseThrow(projectId);
        projectService.update(project);
        return projectId;
    }
}