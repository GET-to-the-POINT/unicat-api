package gettothepoint.unicatapi.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.application.service.OpenAiService;
import gettothepoint.unicatapi.application.service.ProjectService;
import gettothepoint.unicatapi.application.service.SectionService;
import gettothepoint.unicatapi.domain.dto.project.CreateResourceResponse;
import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
import gettothepoint.unicatapi.presentation.controller.projetct.ProjectController;
import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
import gettothepoint.unicatapi.test.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({TestSecurityConfig.class, TestDummyTextToSpeechConfiguration.class, ProjectControllerTest.TestConfig.class})
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpenAiService openAiService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ProjectService projectService() {
            return Mockito.mock(ProjectService.class);
        }

        @Bean
        public SectionService sectionService() {return Mockito.mock(SectionService.class);}

        @Bean
        public OpenAiService openAiService() {return Mockito.mock(OpenAiService.class);}
    }

    @Nested
    @DisplayName("스크립트 생성 테스트")
    class RefineScriptTests {

        @Test
        @DisplayName("유효한 요청일 경우 OK와 예상 응답 반환")
        void testCreateScriptWithOKRequest() throws Exception {
            Long projectId = 1L;
            Long sectionId = 2L;
            PromptRequest scriptRequest = new PromptRequest("원본 스크립트 내용입니다. 20자 이상이에요.");
            CreateResourceResponse expectedResponse = new CreateResourceResponse(null,null,"보정된 스크립트 내용");

            doReturn(expectedResponse).when(openAiService).createScript(projectId, sectionId, scriptRequest);

            mockMvc.perform(post("/projects/" + projectId + "/sections/" + sectionId + "/script")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scriptRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
        }

        @Test
        @DisplayName("20자 미만일 경우 BadRequest 반환")
        void testcreateScriptWithBadRequest() throws Exception {
            Long projectId = 1L;
            Long sectionId = 2L;
            PromptRequest scriptRequest = new PromptRequest("원본 스크립트");
            CreateResourceResponse expectedResponse = new CreateResourceResponse(null,null,"보정된 스크립트 내용");

            doReturn(expectedResponse).when(openAiService).createScript(projectId, sectionId, scriptRequest);

            mockMvc.perform(post("/projects/" + projectId + "/sections/" + sectionId + "/script")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(scriptRequest)))
                    .andExpect(status().isBadRequest());
        }

    }

}
