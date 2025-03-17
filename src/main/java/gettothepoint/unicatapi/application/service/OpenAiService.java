package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.dto.project.CreateImageRequest;
import gettothepoint.unicatapi.domain.dto.project.ImageResponse;
import gettothepoint.unicatapi.domain.dto.project.ScriptRequest;
import gettothepoint.unicatapi.domain.dto.project.ScriptResponse;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;


@Service
public class OpenAiService {

    private final ChatClient chatClient;
    private final SectionRepository sectionRepository;
    private final ProjectRepository projectRepository;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final SupabaseStorageService supabaseStorageService;
    private final OpenAiImageModel openAiImageModel;

    @Autowired
    public OpenAiService(ChatClient.Builder chatClientBuilder, SectionRepository sectionRepository, ProjectRepository projectRepository, AppProperties appProperties, RestTemplate restTemplate, SupabaseStorageService supabaseStorageService, OpenAiImageModel openAiImageModel) {
        this.chatClient = chatClientBuilder.build();
        this.sectionRepository = sectionRepository;
        this.projectRepository = projectRepository;
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
        this.supabaseStorageService = supabaseStorageService;
        this.openAiImageModel = openAiImageModel;
    }

    public ScriptResponse createScript(Long id, Long sectionId, ScriptRequest request) {

        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        String tone = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id))
                .getScriptTone();

        String scriptTone = (tone == null || tone.isBlank()) ? "default" : tone;

        return generateScriptAI(scriptTone, request);

    }

    private ScriptResponse generateScriptAI(String tone, ScriptRequest request) {
        String promptText = String.format(
                appProperties.openAIScript().prompt(),
                tone,
                request.script()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(appProperties.openAIScript().model())
                .temperature(appProperties.openAIScript().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);

        ScriptResponse response = chatClient.prompt()
                .user(prompt.getContents())
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });

        return new ScriptResponse(Objects.requireNonNull(response).script());
    }

    public ImageResponse createImage(Long projectId, Long sectionId, CreateImageRequest createImageRequest) {

        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + sectionId));

        String style = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId))
                .getImageStyle();

        String imageStyle = (style == null || style.isBlank()) ? "default" : style;

        return generateImageAI(imageStyle, createImageRequest);

    }

    private ImageResponse generateImageAI(String imageStyle, CreateImageRequest request) {
        String promptText = String.format(
                appProperties.openAIImage().prompt(),
                imageStyle,
                request.script()
        );

        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model(appProperties.openAIImage().model())
                .quality(appProperties.openAIImage().quality())
                .build();

        org.springframework.ai.image.ImageResponse response = openAiImageModel.call(
                new ImagePrompt(promptText, options));

        Image image = response.getResult().getOutput();

        String alt = String.format("'%s' 내용을 기반으로 AI가 생성한 이미지", request.script());
        String imageUrl = image.getUrl();

        return new ImageResponse(processAndSaveImage(imageUrl), alt);
    }

    private String processAndSaveImage(String imageUrl) {
        URI uri = UriComponentsBuilder.fromUriString(imageUrl).build(true).toUri();
        byte[] imageBytes = restTemplate.getForObject(uri, byte[].class);
        if (imageBytes == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download image from OpenAI");
        }

        MultipartFile multipartFile = new MultipartFileUtil(imageBytes, "download", "image/jpeg");
        return supabaseStorageService.uploadFile(multipartFile);
    }
}
