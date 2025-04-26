package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.config.MailContainer;
import gettothepoint.unicatapi.email.config.SyncMailSenderTestConfig;
import gettothepoint.unicatapi.email.domain.MailMessage;
import gettothepoint.unicatapi.email.domain.MailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static gettothepoint.unicatapi.email.config.CommonConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SyncMailSenderTestConfig.class})
@Testcontainers
@DisplayName("동기 메일 전송 통합 테스트")
class SyncMailSenderIntegrationTest {

    @Container
    static MailContainer mailpit = new MailContainer("axllent/mailpit");

    @DynamicPropertySource
    static void overrideMailProps(DynamicPropertyRegistry registry) {
        mailpit.start();
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", mailpit::getSmtpPort);
    }

    @Autowired
    MailSender mailSender;

    @Test
    @DisplayName("동기 메일이 올바르게 전송되고 수신되는지 검증한다")
    void shouldSendMailSynchronouslyAndVerifyDelivery() {
        MailMessage mailMessage = MailMessage.builder()
                        .recipient(RECIPIENT)
                        .subject(SUBJECT)
                        .content(CONTENT)
                        .isHtml(false)
                        .build();

        mailSender.send(mailMessage);
        System.out.println("메일 전송 완료");

        RestTemplate restTemplate = new RestTemplate();
        String mailpitApiUrl = mailpit.getWebUrl() + "/api/v1/messages";

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    mailpitApiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> response = responseEntity.getBody();
            assertThat(response).isNotNull();
            assertThat(response.get("messages")).isInstanceOfAny(List.class);
            assertThat((List<?>) response.get("messages")).hasSizeGreaterThan(0);
        });
    }
}
