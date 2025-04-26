package gettothepoint.unicatapi.email.config;

import gettothepoint.unicatapi.email.domain.MailSender;
import gettothepoint.unicatapi.email.infrastructure.email.AsyncMailSender;
import gettothepoint.unicatapi.email.infrastructure.email.MailEventListener;
import gettothepoint.unicatapi.email.infrastructure.email.SyncMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;

@TestConfiguration
public class AsyncMailSenderTestConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setDaemon(false);
        return ex;
    }

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

    @Bean
    public MailEventListener mailEventListener(SyncMailSender syncMailSender) {
        return new MailEventListener(syncMailSender);
    }

    @Bean
    @Primary
    public MailSender mailSender(ApplicationEventPublisher publisher) {
        return new AsyncMailSender(publisher);
    }

}
