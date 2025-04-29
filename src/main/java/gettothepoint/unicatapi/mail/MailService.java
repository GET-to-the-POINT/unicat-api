package gettothepoint.unicatapi.mail;

import gettothepoint.unicatapi.member.domain.Member;
import org.springframework.mail.SimpleMailMessage;

import java.net.URI;

public interface MailService {

    void handleSendMailEvent(SimpleMailMessage simpleMailMessage);

    void send(SimpleMailMessage simpleMailMessage);

    void confirmEmail(Member member, URI uri);

    void changedPassword(Member member);
}
