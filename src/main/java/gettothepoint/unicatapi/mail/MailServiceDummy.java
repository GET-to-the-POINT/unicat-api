package gettothepoint.unicatapi.mail;

import gettothepoint.unicatapi.member.domain.Member;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class MailServiceDummy implements MailService {

    @Override
    public void handleSendMailEvent(SimpleMailMessage simpleMailMessage) {}

    @Override
    public void send(SimpleMailMessage simpleMailMessage) {}

    @Override
    public void confirmEmail(Member member, URI uri) {}

    @Override
    public void changedPassword(Member member) {}
}
