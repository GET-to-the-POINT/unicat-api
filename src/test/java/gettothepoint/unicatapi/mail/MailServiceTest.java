package gettothepoint.unicatapi.mail;

import gettothepoint.unicatapi.mail.config.MailContainer;
import gettothepoint.unicatapi.mail.config.MailServiceTestConfig;
import gettothepoint.unicatapi.mail.config.SyncTaskExecutorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 비동기 메일 전송 통합 테스트
 * Mailpit 컨테이너를 사용해 비동기 메일 전송과 수신을 검증합니다.
 */
@SpringJUnitConfig(classes = {MailServiceTestConfig.class, MailServiceImpl.class, SyncTaskExecutorConfig.class})
@Testcontainers
@DisplayName("비동기 메일 전송 통합 테스트")
class MailServiceTest {
    private static final String RECIPIENT = "test@test.com";
    private static final String SUBJECT = "Test Subject";
    private static final String CONTENT = "Test Content";

    @Container
    static MailContainer mailpit = new MailContainer("axllent/mailpit");
    @Autowired
    MailService service;

    @DynamicPropertySource
    static void overrideMailProps(DynamicPropertyRegistry registry) {
        mailpit.start();
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", mailpit::getSmtpPort);
    }

    @Test
    @DisplayName("비동기 메일이 올바르게 전송되고 수신되는지 검증한다")
    void shouldSendMailAsynchronouslyAndVerifyDelivery() {
        // 테스트용 메일 메시지 생성
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(RECIPIENT);
        simpleMailMessage.setSubject(SUBJECT);
        simpleMailMessage.setText(CONTENT);

        // 메일 전송
        service.send(simpleMailMessage);

        // 메일 수신 확인
        RestTemplate restTemplate = new RestTemplate();
        String mailpitApiUrl = mailpit.getWebUrl() + "/api/v1/messages";

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(mailpitApiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
            Map<String, Object> response = responseEntity.getBody();
            assertThat(response).isNotNull();
            assertThat(response.get("messages")).isInstanceOfAny(List.class);
            assertThat((List<?>) response.get("messages")).hasSizeGreaterThan(0);
        });
    }
}
