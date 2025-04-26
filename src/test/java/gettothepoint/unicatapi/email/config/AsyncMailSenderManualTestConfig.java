package gettothepoint.unicatapi.email.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@TestConfiguration
public class AsyncMailSenderManualTestConfig {

    @Bean
    public Executor taskExecutor() {
        return new SyncTaskExecutor();
    }

}
