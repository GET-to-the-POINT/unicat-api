package gettothepoint.unicatapi.test.config;

import gettothepoint.unicatapi.application.service.email.EmailService;
import gettothepoint.unicatapi.domain.entity.member.Member;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDummyEmailServiceConfiguration {

    @Bean
    @Primary
    public EmailService emailService() {
        return new EmailService(null, null, null) {
            @Override
            public void send(String recipient, String subject, String content) {
                // 테스트에서는 실제 이메일 전송을 수행하지 않음
            }

            @Override
            public void sendVerificationEmail(Member member) {
                // 테스트에서는 실제 이메일 전송을 수행하지 않음
            }
        };
    }
}