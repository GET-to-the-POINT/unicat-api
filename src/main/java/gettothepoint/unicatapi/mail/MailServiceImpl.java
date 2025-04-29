package gettothepoint.unicatapi.mail;

import gettothepoint.unicatapi.common.util.JwtUtil;
import gettothepoint.unicatapi.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Profile("mail")
@Primary
public class MailServiceImpl implements MailService {

    private final ApplicationEventPublisher publisher;
    private final JavaMailSender javaMailSender;
    private final JwtUtil jwtUtil;

    @Async
    @EventListener
    public void handleSendMailEvent(SimpleMailMessage simpleMailMessage) {
        javaMailSender.send(simpleMailMessage);
    }

    @Override
    public void send(SimpleMailMessage simpleMailMessage) {
        publisher.publishEvent(simpleMailMessage);
    }

    @Override
    public void confirmEmail(Member member, URI uri) {
        String token = jwtUtil.generateJwtToken(member.getId());

        String confirmUrl = UriComponentsBuilder
                .fromUri(uri)
                .queryParam("token", token)
                .build()
                .toUriString();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(member.getEmail());
        simpleMailMessage.setSubject("Email Confirmation");
        simpleMailMessage.setText("Please confirm your email address: " + confirmUrl);
        send(simpleMailMessage);
    }

    @Override
    public void changedPassword(Member member) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(member.getEmail());
        simpleMailMessage.setSubject("Password Changed");
        simpleMailMessage.setText("Your password has been changed successfully.");
        send(simpleMailMessage);
    }
}