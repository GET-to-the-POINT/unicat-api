package gettothepoint.unicatapi.mail;

import org.springframework.mail.SimpleMailMessage;

public interface MailService {
    void send(SimpleMailMessage simpleMailMessage);
}
