package gettothepoint.unicatapi.mail.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration
public class SyncTaskExecutorConfig {

    /**
     * 동기식 작업 실행기를 생성하여 비동기 작업을 동기식으로 처리합니다.
     * 이를 통해 테스트에서 비동기 작업의 완료를 기다릴 필요 없이 즉시 결과를 확인할 수 있습니다.
     *
     * @return 동기식 실행기 인스턴스
     */
    @Bean
    public Executor taskExecutor() {
        return new SyncTaskExecutor();
    }
}
