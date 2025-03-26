package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.application.service.project.UsageLimitService;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.dto.project.CreateResourceResponse;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class OpenAiService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final StorageService storageService;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiChatModel openAiChatModel;
    private final UsageLimitService usageLimitService;
    private final ProjectService projectService;
    private final SectionService sectionService;

    public CreateResourceResponse createScript(Long projectId, Long sectionId, PromptRequest request) {
        Project project = projectService.getOrElseThrow(projectId);
        Section section = sectionService.getOrElseThrow(sectionId);
        usageLimitService.incrementUsage(project.getMember().getId(), "script");

        String tone = project.getScriptTone();
        String scriptTone = (tone == null || tone.isBlank()) ? "default" : tone;

        CreateResourceResponse scriptResponse = generateScriptAI(scriptTone, request);
        section.setScript(scriptResponse.script());
        sectionService.update(section);

        return scriptResponse;

    }

    private CreateResourceResponse generateScriptAI(String tone, PromptRequest request) {
        String promptText = String.format(
                appProperties.openAIScript().prompt(),
                tone,
                request.prompt()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(appProperties.openAIScript().model())
                .temperature(appProperties.openAIScript().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);

        String script = ChatClient.create(openAiChatModel)
                        .prompt()
                        .user(prompt.getContents())
                        .call()
                        .entity(String.class);

        return new CreateResourceResponse(null, null, script);
    }

    public CreateResourceResponse createImage(Long projectId, Long sectionId, PromptRequest scriptRequest) {
        Project project = projectService.getOrElseThrow(projectId);
        Section section = sectionService.getOrElseThrow(sectionId);
        usageLimitService.incrementUsage(project.getMember().getId(), "image");

        String style = project.getImageStyle();
        String imageStyle = (style == null || style.isBlank()) ? "Photo" : style;

        CreateResourceResponse imageResponse = generateImageAI(imageStyle, scriptRequest);
        saveImageToSection(section, imageResponse);
        sectionService.update(section);

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

    private void saveImageToSection(Section section, CreateResourceResponse imageResponse) {
        String imageUrl = imageResponse.imageUrl();
        String alt = imageResponse.alt();

        section.setResourceUrl(imageUrl);
        section.setAlt(alt);
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
}
