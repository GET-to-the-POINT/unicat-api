package gettothepoint.unicatapi.email.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 테스트에서 사용되는 공통 메일 설정을 제공하는 클래스.
 * 모든 메일 발송 테스트 구성에서 재사용할 수 있는 JavaMailSender 구성을 포함합니다.
 */
@TestConfiguration
public class CommonMailConfig {

    /** 테스트 이메일 수신자 주소 */
    public static final String RECIPIENT = "test@test.com";

    /** 테스트 이메일 제목 */
    public static final String SUBJECT = "Test Subject";

    /** 테스트 이메일 내용 */
    public static final String CONTENT = "Test Content";

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    /**
     * 테스트용 JavaMailSender 빈을 생성합니다.
     * 인증이 필요없는 메일 서버 설정과 디버그 모드가 활성화되어 있습니다.
     *
     * @return 설정된 JavaMailSender 인스턴스
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername("");
        mailSender.setPassword("");

        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "false");
        props.setProperty("mail.smtp.starttls.enable", "false");
        props.setProperty("mail.debug", "true");
        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}
