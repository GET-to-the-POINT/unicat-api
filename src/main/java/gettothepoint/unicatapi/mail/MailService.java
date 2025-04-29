package gettothepoint.unicatapi.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("mail")
@Primary
public class MailService {

    private final ApplicationEventPublisher publisher;

    private final JavaMailSender javaMailSender;

    @Async
    @EventListener
    public void handleSendMailEvent(SimpleMailMessage simpleMailMessage) {
        javaMailSender.send(simpleMailMessage);
    }

    public void send(SimpleMailMessage simpleMailMessage) {
        publisher.publishEvent(simpleMailMessage);
    }
}