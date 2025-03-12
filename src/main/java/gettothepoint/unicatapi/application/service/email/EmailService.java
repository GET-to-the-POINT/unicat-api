package gettothepoint.unicatapi.application.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    public void sendEmail(String toEmail, String title, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(title);
        helper.setText(content, true);
        helper.setReplyTo("imgforest@gmail.com");
        try {
            emailSender.send(message);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to send email in sendEmail", e);
        }
    }

//    public SimpleMailMessage createEmailForm(String toEmail, String title, String text){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject(title);
//        message.setText(text);
//        return message;
}
