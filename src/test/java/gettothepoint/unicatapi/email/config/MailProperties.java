package gettothepoint.unicatapi.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "test.mail")
public record MailProperties(String host, int port) {}