package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.artifact.application.ProjectService;
import gettothepoint.unicatapi.artifact.application.SectionService;
import gettothepoint.unicatapi.common.properties.OpenAIProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.ai.domain.dto.AutoArtifact;
import gettothepoint.unicatapi.ai.domain.dto.CreateResourceResponse;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import gettothepoint.unicatapi.artifact.domain.Section;
import gettothepoint.unicatapi.filestorage.application.FileService;
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

    private final OpenAIProperties openAIProperties;
    private final RestTemplate restTemplate;
    private final FileService fileService;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiChatModel openAiChatModel;
    private final ProjectService projectService;
    private final SectionService sectionService;

    public CreateResourceResponse createScript(Long projectId, Long sectionId, PromptRequest request) {

        String tone = projectService.getOrElseThrow(projectId).getScriptTone();

        Section section = sectionService.getOrElseThrow(sectionId);

        String scriptTone = (tone == null || tone.isBlank()) ? "default" : tone;

        CreateResourceResponse scriptResponse = generateScriptAI(scriptTone, request);
        section.setScript(scriptResponse.script());
        sectionService.update(section);

        return scriptResponse;

    }

    private CreateResourceResponse generateScriptAI(String tone, PromptRequest request) {
        String promptText = String.format(
                openAIProperties.script().prompt(),
                tone,
                request.prompt()
        );
        log.debug("generateScriptAI - Prompt created: {}", promptText);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openAIProperties.script().model())
                .temperature(openAIProperties.script().temperature())
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

        return new CreateResourceResponse(null, script);
    }

    public CreateResourceResponse createImage(Long projectId, Long sectionId, PromptRequest scriptRequest) {
        log.info("createImage called with projectId: {}, sectionId: {}", projectId, sectionId);
        String style = projectService.getOrElseThrow(projectId).getImageStyle();
        sectionService.getOrElseThrow(sectionId);
        log.debug("Section {} validated", sectionId);

        String imageStyle = (style == null || style.isBlank()) ? "Photo" : style;

        CreateResourceResponse imageResponse = generateImageAI(imageStyle, scriptRequest);

        saveImageToSection(sectionId, imageResponse.imageUrl());
        return imageResponse;
    }

    private CreateResourceResponse generateImageAI(String imageStyle, PromptRequest request) {
        String promptText = String.format(
                openAIProperties.image().prompt(),
                imageStyle,
                request.prompt()
        );
        log.info("generateImageAI - Prompt created: {}", promptText);

        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model(openAIProperties.image().model())
                .quality(openAIProperties.image().quality())
                .build();

        org.springframework.ai.image.ImageResponse response = openAiImageModel.call(
                new ImagePrompt(promptText, options));

        Image image = response.getResult().getOutput();

        String imageUrl = image.getUrl();

        return new CreateResourceResponse(processAndUploadImage(imageUrl), null);
    }

    private String processAndUploadImage(String imageUrl) {
        URI uri = UriComponentsBuilder.fromUriString(imageUrl).build(true).toUri();
        byte[] imageBytes = restTemplate.getForObject(uri, byte[].class);
        if (imageBytes == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download multipartFile from OpenAI");
        }

        MultipartFile multipartFile = new MultipartFileUtil(imageBytes, "download", "image/jpeg");
        return fileService.uploadFile(multipartFile);
    }

    private void saveImageToSection(Long sectionId, String imageUrl) {
        Section section = sectionService.getOrElseThrow(sectionId);

        section.setContentKey(imageUrl);
        sectionService.update(section);
    }

    public CreateResourceResponse createResource(Long projectId, Long sectionId, String type, PromptRequest promptRequest) {
        if ("image".equalsIgnoreCase(type)) {
            return createImage(projectId, sectionId, promptRequest);
        } else if ("script".equalsIgnoreCase(type)) {
            return createScript(projectId, sectionId, promptRequest);
        } else {
            CreateResourceResponse imageResponse = createImage(projectId, sectionId, promptRequest);
            CreateResourceResponse scriptResponse = createScript(projectId, sectionId, promptRequest);
            return new CreateResourceResponse(imageResponse.imageUrl(), scriptResponse.script());
        }
    }

    public void oneStepCreateResource(Long projectId, PromptRequest request) {
        List<Section> sections = sectionService.getSectionAll(projectId);

        String tone = projectService.getOrElseThrow(projectId).getScriptTone();

        String promptText = String.format(
                openAIProperties.auto().prompt(),
                tone,
                request.prompt()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openAIProperties.script().model())
                .temperature(openAIProperties.script().temperature())
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
            sectionService.update(section);
            createImage(projectId, section.getId(), new PromptRequest(script));
        }
    }
}