//package gettothepoint.unicatapi.presentation.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import gettothepoint.unicatapi.application.service.OpenAiService;
//import gettothepoint.unicatapi.application.service.project.ProjectService;
//import gettothepoint.unicatapi.application.service.project.SectionService;
//import gettothepoint.unicatapi.domain.dto.project.CreateResourceResponse;
//import gettothepoint.unicatapi.domain.dto.project.PromptRequest;
//import gettothepoint.unicatapi.domain.dto.project.ProjectResponse;
//import gettothepoint.unicatapi.presentation.controller.project.ProjectController;
//import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
//import gettothepoint.unicatapi.test.config.TestSecurityConfig;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.doThrow;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ProjectController.class)
//@Import({TestSecurityConfig.class, TestDummyTextToSpeechConfiguration.class, ProjectControllerTest.TestConfig.class})
//public class ProjectControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private OpenAiService openAiService;
//
//    @Autowired
//    private ProjectService projectService; // ✅ 추가
//
//    @TestConfiguration
//    static class TestConfig {
//
//        @Bean
//        public ProjectService projectService() {
//            return Mockito.mock(ProjectService.class);
//        }
//
//        @Bean
//        public SectionService sectionService() {
//            return Mockito.mock(SectionService.class);
//        }
//
//        @Bean
//        public OpenAiService openAiService() {
//            return Mockito.mock(OpenAiService.class);
//        }
//    }
//
//    @Nested
//    @DisplayName("스크립트 생성 테스트")
//    class RefineScriptTests {
//
//        @Test
//        @DisplayName("유효한 요청일 경우 OK와 예상 응답 반환")
//        void testCreateScriptWithOKRequest() throws Exception {
//            Long projectId = 1L;
//            Long sectionId = 2L;
//            PromptRequest scriptRequest = new PromptRequest("원본 스크립트 내용입니다. 20자 이상이에요.");
//            CreateResourceResponse expectedResponse = new CreateResourceResponse(null,null,"보정된 스크립트 내용");
//
//            doReturn(expectedResponse).when(openAiService).createScript(projectId, sectionId, scriptRequest);
//
//            mockMvc.perform(post("/projects/" + projectId + "/sections/" + sectionId + "/script")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(scriptRequest)))
//                    .andExpect(status().isOk())
//                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
//        }
//
//        @Test
//        @DisplayName("20자 미만일 경우 BadRequest 반환")
//        void testcreateScriptWithBadRequest() throws Exception {
//            Long projectId = 1L;
//            Long sectionId = 2L;
//            PromptRequest scriptRequest = new PromptRequest("원본 스크립트");
//            CreateResourceResponse expectedResponse = new CreateResourceResponse(null,null,"보정된 스크립트 내용");
//
//            doReturn(expectedResponse).when(openAiService).createScript(projectId, sectionId, scriptRequest);
//
//            mockMvc.perform(post("/projects/" + projectId + "/sections/" + sectionId + "/script")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(scriptRequest)))
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("📌 프로젝트 단일 조회 테스트")
//    class GetProjectTests {
//
//        @Test
//        @DisplayName("✅ 유효한 프로젝트 ID일 경우 OK와 예상 응답 반환")
//        void testGetProjectWithValidId() throws Exception {
//            // Given
//            Long projectId = 1L;
//            ProjectResponse expectedResponse = new ProjectResponse(
//                    projectId,
//                    "테스트 프로젝트",
//                    "테스트 부제목",
//                    "https://example.com/image.png",
//                    "https://example.com/video.mp4",
//                    "설정된 톤",
//                    "설정된 이미지 스타일",
//                    LocalDateTime.now()
//            );
//            System.out.println("✅ expectedResponse: " + expectedResponse);
//            // ✅ Mock 설정 확인
//            doReturn(expectedResponse).when(projectService).get(projectId);
//
//            // ✅ 컨트롤러와 같은 URL 요청
//            mockMvc.perform(get("/projects/" + projectId)
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andExpect(content().json(objectMapper.writeValueAsString(expectedResponse)));
//        }
//
//        @Test
//        @DisplayName("🚨 존재하지 않는 프로젝트 ID일 경우 NotFound 반환")
//        void testGetProjectWithInvalidId() throws Exception {
//            // Given
//            Long invalidProjectId = 999L;
//
//            // Mock 설정: 존재하지 않는 프로젝트 ID 요청 시 예외 발생
//            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "요청한 프로젝트를 찾을 수 없습니다."))
//                    .when(projectService).get(invalidProjectId);
//
//            // When & Then
//            mockMvc.perform(get("/project/" + invalidProjectId)
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isNotFound());
//        }
//        @Test
//        @DisplayName("🚨 요청이 유효하지 않을 경우 400 Bad Request 반환")
//        void testGetProjectWithInvalidRequest() throws Exception {
//            // Given: projectId에 문자를 넣으면 잘못된 요청
//            String invalidProjectId = "invalid";
//
//            // When & Then
//            mockMvc.perform(get("/projects/" + invalidProjectId)
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andDo(print()) // ✅ 요청 & 응답 확인
//                    .andExpect(status().isBadRequest()); // ✅ HTTP 400 확인
//        }
//    }
//}
