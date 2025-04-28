package gettothepoint.unicatapi.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailServiceDummy implements MailService {
    @Override
    public void send(SimpleMailMessage simpleMailMessage) {
        // Dummy implementation: do nothing
    }
}
