package gettothepoint.unicatapi.email.config;

import gettothepoint.unicatapi.email.infrastructure.email.AsyncMailSender;
import gettothepoint.unicatapi.email.infrastructure.email.MailEventListener;
import gettothepoint.unicatapi.email.infrastructure.email.SyncMailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 비동기 메일 발송 테스트를 위한 설정 클래스.
 * 실제 비동기 처리가 일어나는 환경을 시뮬레이션합니다.
 */
@TestConfiguration
@Import({CommonMailConfig.class, SyncMailSender.class, MailEventListener.class, AsyncMailSender.class, SyncTaskExecutorTestConfig.class})
public class AsyncMailSenderTestConfig {

}
