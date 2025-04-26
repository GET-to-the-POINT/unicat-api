package gettothepoint.unicatapi.email.config;

import gettothepoint.unicatapi.email.infrastructure.email.SyncMailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 동기식 메일 발송 테스트를 위한 설정 클래스.
 * 메일 발송이 즉시 처리되는 환경을 시뮬레이션합니다.
 */
@TestConfiguration
@Import({CommonMailConfig.class, SyncMailSender.class})
public class SyncMailSenderTestConfig {
}
