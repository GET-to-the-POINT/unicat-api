package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.config.MailContainer;
import gettothepoint.unicatapi.email.domain.MailMessage;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static gettothepoint.unicatapi.email.config.CommonMailConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 메일 전송 테스트를 위한 공통 추상 클래스
 * 모든 메일 전송 테스트에서 사용되는 공통 메서드와 유틸리티를 제공합니다.
 */
public abstract class AbstractMailSenderTest {

    /**
     * 테스트용 메일 메시지를 생성합니다.
     *
     * @return 생성된 메일 메시지 객체
     */
    protected MailMessage createTestMailMessage() {
        return MailMessage.builder()
                .recipient(RECIPIENT)
                .subject(SUBJECT)
                .content(CONTENT)
                .isHtml(false)
                .build();
    }

    /**
     * Mailpit API를 통해 메일이 수신되었는지 검증합니다.
     *
     * @param mailContainer 메일 테스트 컨테이너
     */
    protected void verifyMailDelivery(MailContainer mailContainer) {
        RestTemplate restTemplate = new RestTemplate();
        String mailpitApiUrl = mailContainer.getWebUrl() + "/api/v1/messages";

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

    /**
     * 메일이 전송되었다는 로그 메시지를 출력합니다.
     */
    protected void logMailSent() {
        System.out.println("메일 전송 완료");
    }
}
