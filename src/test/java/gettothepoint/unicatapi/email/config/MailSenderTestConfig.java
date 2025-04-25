package gettothepoint.unicatapi.email.config;

import gettothepoint.unicatapi.email.domain.MailSender;
import gettothepoint.unicatapi.email.infrastructure.email.MailSenderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@TestConfiguration
@EnableConfigurationProperties(MailProperties.class)
public class MailSenderTestConfig {

    @Autowired
    MailProperties mailProperties;

    public static final String RECIPIENT = "recipient@test.com";
    public static final String SUBJECT = "Test Subject";
    public static final String CONTENT = "Test Content";

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.host());
        mailSender.setPort(mailProperties.port());
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
    public MailSender mailSender(JavaMailSender javaMailSender) {
        return new MailSenderImpl(javaMailSender);
    }
}
