package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.dto.project.*;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class OpenAiService {

    private final SectionRepository sectionRepository;
    private final ProjectRepository projectRepository;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final StorageService storageService;
    private final OpenAiImageModel openAiImageModel;
    private static final String SECTION_NOT_FOUND_MSG = "Section not found with id: ";
    private final OpenAiChatModel openAiChatModel;
    private static final String PROJECT_NOT_FOUND_MSG = "Project not found with id: ";

    public CreateResourceResponse createScript(Long id, Long sectionId, PromptRequest request) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        String tone = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PROJECT_NOT_FOUND_MSG + id))
                .getScriptTone();

        String scriptTone = (tone == null || tone.isBlank()) ? "default" : tone;

        CreateResourceResponse scriptResponse = generateScriptAI(scriptTone, request);
        section.setScript(scriptResponse.script());
        sectionRepository.save(section);

        return scriptResponse;

    }

    private CreateResourceResponse generateScriptAI(String tone, PromptRequest request) {
        String promptText = String.format(
                appProperties.openAIScript().prompt(),
                tone,
                request.prompt()
        );
        log.debug("generateScriptAI - Prompt created: {}", promptText);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(appProperties.openAIScript().model())
                .temperature(appProperties.openAIScript().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);

        String raw = ChatClient.create(openAiChatModel)
                .prompt()
                .user(prompt.getContents())
                .call()
                .content();
        log.info("Received response from OpenAiChatModel for script generation");

        String script = Optional.ofNullable(raw)
                .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                .orElse("");

        return new CreateResourceResponse(null, null, script);
    }

    public CreateResourceResponse createImage(Long projectId, Long sectionId, PromptRequest scriptRequest) {
        log.info("createImage called with projectId: {}, sectionId: {}", projectId, sectionId);

        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));
        log.debug("Section {} validated", sectionId);

        String style = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(PROJECT_NOT_FOUND_MSG + projectId))
                .getImageStyle();

        String imageStyle = (style == null || style.isBlank()) ? "Photo" : style;

        CreateResourceResponse imageResponse = generateImageAI(imageStyle, scriptRequest);

        saveImageToSection(sectionId, imageResponse.imageUrl(), imageResponse.alt());
        return imageResponse;
    }

    private CreateResourceResponse generateImageAI(String imageStyle, PromptRequest request) {
        String promptText = String.format(
                appProperties.openAIImage().prompt(),
                imageStyle,
                request.prompt()
        );

        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model(appProperties.openAIImage().model())
                .quality(appProperties.openAIImage().quality())
                .build();

        org.springframework.ai.image.ImageResponse response = openAiImageModel.call(
                new ImagePrompt(promptText, options));

        Image image = response.getResult().getOutput();

        String alt = String.format("'%s' 내용을 기반으로 AI가 생성한 이미지", request.prompt());
        String imageUrl = image.getUrl();

        return new CreateResourceResponse(processAndUploadImage(imageUrl), alt, null);
    }

    private String processAndUploadImage(String imageUrl) {
        URI uri = UriComponentsBuilder.fromUriString(imageUrl).build(true).toUri();
        byte[] imageBytes = restTemplate.getForObject(uri, byte[].class);
        if (imageBytes == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download multipartFile from OpenAI");
        }

        MultipartFile multipartFile = new MultipartFileUtil(imageBytes, "download", "image/jpeg");
        return storageService.upload(multipartFile);
    }

    private void saveImageToSection(Long sectionId, String imageUrl, String alt) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        section.setContentUrl(imageUrl);
        section.setAlt(alt);
        sectionRepository.save(section);
    }

    public CreateResourceResponse createResource(Long projectId, Long sectionId, String type, PromptRequest promptRequest) {
        if ("image".equalsIgnoreCase(type)) {
            return createImage(projectId, sectionId, promptRequest);
        } else if ("script".equalsIgnoreCase(type)) {
            return createScript(projectId, sectionId, promptRequest);
        } else {
            CreateResourceResponse imageResponse = createImage(projectId, sectionId, promptRequest);
            CreateResourceResponse scriptResponse = createScript(projectId, sectionId, promptRequest);
            return new CreateResourceResponse(imageResponse.imageUrl(), imageResponse.alt(), scriptResponse.script());
        }
    }

    public void oneStepCreateResource(Long projectId, PromptRequest request) {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);

        String tone = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(PROJECT_NOT_FOUND_MSG + projectId))
                .getScriptTone();

        String promptText = String.format(
                appProperties.openAIAuto().prompt(),
                tone,
                request.prompt()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(appProperties.openAIScript().model())
                .temperature(appProperties.openAIScript().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);

        AutoArtifact autoArtifact = ChatClient.create(openAiChatModel).prompt()
                .user(prompt.getContents())
                .call()
                .entity(AutoArtifact.class);

        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            assert autoArtifact != null;
            String script = autoArtifact.scripts().get(i);
            section.setScript(script);
            sectionRepository.save(section);
            createImage(projectId, section.getId(), new PromptRequest(script));
        }
    }
}