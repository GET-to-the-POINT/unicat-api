package gettothepoint.unicatapi.application.service;

import gettothepoint.unicatapi.application.service.ai.OpenAiService;
import gettothepoint.unicatapi.common.propertie.OpenAIProperties;
import gettothepoint.unicatapi.domain.dto.project.CreateResourceResponse;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.domain.entity.project.Project;
import gettothepoint.unicatapi.domain.entity.project.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OpenAIProperties openAIProperties;

    @Mock
    private OpenAIProperties.Script openAI;

    @Mock
    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(openAIProperties.script()).thenReturn(openAI);

    }

    @Test
    void testCreateScriptSuccess() {
        Long projectId = 1L;
        Long sectionId = 2L;
        PromptRequest scriptRequest = new PromptRequest("원본 스크립트 내용");

        Section section = new Section();
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));

        Project project = new Project();
        project.setScriptTone("friendly");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        when(openAI.prompt()).thenReturn("Tone: %s, Script: %s");
        when(openAI.model()).thenReturn("gpt-4o-mini");
        when(openAI.temperature()).thenReturn(0.7);

        String expectedPrompt = String.format("Tone: %s, Script: %s", "friendly", scriptRequest.prompt());

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(expectedPrompt)).thenReturn(chatClientRequestSpec);

        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        CreateResourceResponse dummyResponse = new CreateResourceResponse(null, null,"보정된 스크립트 내용");
        when(callResponseSpec.entity(ArgumentMatchers.<ParameterizedTypeReference<CreateResourceResponse>>any()))
                .thenReturn(dummyResponse);

        CreateResourceResponse response = openAiService.createScript(projectId, sectionId, scriptRequest);
        assertNotNull(response);
        assertEquals("보정된 스크립트 내용", response.script());

        verify(sectionRepository).findById(sectionId);
        verify(projectRepository).findById(projectId);
        verify(chatClientRequestSpec).user(expectedPrompt);
        verify(chatClientRequestSpec).call();
    }
}
