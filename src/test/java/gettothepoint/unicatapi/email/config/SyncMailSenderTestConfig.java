package gettothepoint.unicatapi.email.config;

import gettothepoint.unicatapi.email.domain.MailSender;
import gettothepoint.unicatapi.email.infrastructure.email.SyncMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@TestConfiguration
public class SyncMailSenderTestConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

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

    @Bean
    public MailSender syncMailSender(JavaMailSender javaMailSender) {
        return new SyncMailSender(javaMailSender);
    }

}
