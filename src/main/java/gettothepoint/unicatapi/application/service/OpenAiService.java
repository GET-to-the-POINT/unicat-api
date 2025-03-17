package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.dto.project.*;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
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

    private final SectionRepository sectionRepository;
    private final ProjectRepository projectRepository;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final SupabaseStorageService supabaseStorageService;
    private final OpenAiImageModel openAiImageModel;
    private static final String SECTION_NOT_FOUND_MSG = "Section not found with id: ";
    private final OpenAiChatModel openAiChatModel;

    public CreateResourceResponse createScript(Long id, Long sectionId, PromptRequest request) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        String tone = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id))
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

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(appProperties.openAIScript().model())
                .temperature(appProperties.openAIScript().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);
        ChatResponse response = openAiChatModel.call(prompt);
        AssistantMessage assistantMessage = response.getResult().getOutput();

        return new CreateResourceResponse(null, null, assistantMessage.getText());
    }

    public CreateResourceResponse createImage(Long projectId, Long sectionId, PromptRequest scriptRequest) {

        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        String style = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId))
                .getImageStyle();

        String imageStyle = (style == null || style.isBlank()) ? "default" : style;

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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download image from OpenAI");
        }

        MultipartFile multipartFile = new MultipartFileUtil(imageBytes, "download", "image/jpeg");
        return supabaseStorageService.uploadFile(multipartFile);
    }

    private void saveImageToSection(Long sectionId, String imageUrl, String alt) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException(SECTION_NOT_FOUND_MSG + sectionId));

        section.setUploadImageUrl(imageUrl);
        section.setAlt(alt);
        sectionRepository.save(section);
    }

    public CreateResourceResponse createContent(Long projectId, Long sectionId, String type, PromptRequest promptRequest) {

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
